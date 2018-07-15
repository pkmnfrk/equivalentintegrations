package com.mike_caron.equivalentintegrations.block.transmutation_generator;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.api.capabilities.IEMCManager;
import com.mike_caron.equivalentintegrations.block.TransmutationTileEntityBase;
import com.mike_caron.equivalentintegrations.impl.EMCManagerProvider;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransmutationGeneratorTileEntity
        extends TransmutationTileEntityBase
        implements ITickable, IEnergyStorage
{
    boolean generating = false;
    boolean generatedLastTick = false;

    int powerPerTick = 10;
    int buffer;

    @Override
    @Nonnull
    protected ItemStackHandler createInventory()
    {
        return new TransmutationGeneratorStackHandler()
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                if(world.isRemote)
                    return;

                ItemStack stack = this.getStackInSlot(0);
                UUID owner = SoulboundTalisman.getOwnerFromStack(stack);

                TransmutationGeneratorTileEntity.this.setOwner(owner);
                TransmutationGeneratorTileEntity.this.markDirty();

                //TransmutationGeneratorTileEntity.this.setTransmutationParameters();
            }
        };
    }

    public boolean isGenerating()
    {
        return generatedLastTick;
    }

    @Override
    protected void onNewOwner()
    {
        generating = owner != null;
        if(!generating) generatedLastTick = false;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    private float getEfficiency()
    {
        ItemStack stack = inventory.getStackInSlot(1);

        return getEfficiency(stack.getCount());
    }

    public static float getEfficiency(int num)
    {
        switch(num)
        {
            case 0: return 0.5f;
            case 1: return 1f;
            case 2: return 2f;
            default: return 3f;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey("generating"))
        {
            generating = compound.getBoolean("generating");
        }
        if(compound.hasKey("buffer"))
        {
            buffer = compound.getInteger("buffer");
        }
        if(compound.hasKey("powerPerTick"))
        {
            powerPerTick = compound.getInteger("powerPerTick");
        }
        if(compound.hasKey("generatedLastTick"))
        {
            generatedLastTick = compound.getBoolean("generatedLastTick");
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setBoolean("generatedLastTick", generatedLastTick);
        ret.setBoolean("generating", generating);
        ret.setInteger("buffer", buffer);
        ret.setInteger("powerPerTick", powerPerTick);
        return ret;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        EquivalentIntegrationsMod.logger.info("Handling updating tag: {}", tag);
        super.handleUpdateTag(tag);
    }

    @Override
    public void update()
    {
        if(world.isRemote) return;

        boolean needsUpdate;

        needsUpdate = pushEnergy();
        needsUpdate = generateEnergy() || needsUpdate;

        if(needsUpdate)
        {
            EquivalentIntegrationsMod.logger.info("Requesting update for Generator");
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private boolean pushEnergy()
    {
        if(buffer <= 0)
            return false;

        List<IEnergyStorage> toSend = new ArrayList<>();

        for(EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos neighbour = getPos().offset(facing);
            EnumFacing theirFacing = facing.getOpposite();

            TileEntity other = world.getTileEntity(neighbour);
            if(other == null)
                continue;

            if(!other.hasCapability(CapabilityEnergy.ENERGY, theirFacing))
                continue;

            IEnergyStorage energyStorage = other.getCapability(CapabilityEnergy.ENERGY, theirFacing);
            if(!energyStorage.canReceive())
                continue;

            toSend.add(energyStorage);
        }

        if(toSend.size() > 0)
        {
            int perBuffer = Math.floorDiv(buffer, toSend.size());

            if(perBuffer < 1)
                perBuffer = 1;

            for(IEnergyStorage energyStorage : toSend)
            {
                if(buffer <= 0) break;
                if(perBuffer < buffer)
                    perBuffer = buffer;

                int sent = energyStorage.receiveEnergy(perBuffer, false);
                buffer -= sent;
            }
        }

        return false;
    }

    private boolean generateEnergy()
    {
        boolean prevGenerating = isGenerating();

        generatedLastTick = false;

        if(generating && owner != null)
        {

            float energyModifier = getEfficiency();

            int energyToGet = getMaxEnergyStored() - buffer;
            if (energyToGet > powerPerTick)
            {
                energyToGet = powerPerTick;
            }
            if (energyToGet > 0)
            {
                long emcToConsume = (long) Math.ceil(energyToGet / energyModifier);
                energyToGet = (int)Math.floor(emcToConsume * energyModifier);

                //validate that we aren't wasting EMC
                if(energyToGet + buffer <= getMaxEnergyStored())
                {

                    IEMCManager emcManager = world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

                    double emc = emcManager.getEMC(owner);

                    if (emcToConsume > emc)
                    {
                        energyToGet = (int) Math.floor(emc * energyModifier);
                        emcToConsume = (long) Math.ceil(energyToGet / energyModifier);
                    }

                    if (emcToConsume > 0)
                    {

                        emc -= emcToConsume;

                        emcManager.setEMC(owner, emc);

                        buffer += energyToGet;

                        generatedLastTick = true;
                        markDirty();
                    }
                }
            }
        }

        return isGenerating() != prevGenerating;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        int actualExtract = maxExtract;
        if(actualExtract > buffer) {
            actualExtract = buffer;
        }

        if(!simulate) {
            buffer -= actualExtract;
        }

        return actualExtract;
    }

    @Override
    public int getEnergyStored()
    {
        return buffer;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return Math.max(buffer, powerPerTick * 20 * 10);
    }

    @Override
    public boolean canExtract()
    {
        return true;
    }

    @Override
    public boolean canReceive()
    {
        return false;
    }
}
