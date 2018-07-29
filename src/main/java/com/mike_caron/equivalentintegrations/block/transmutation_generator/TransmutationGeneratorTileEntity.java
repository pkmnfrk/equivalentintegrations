package com.mike_caron.equivalentintegrations.block.transmutation_generator;

import com.mike_caron.equivalentintegrations.ModConfig;
import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.TransmutationTileEntityBase;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
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
    double internalEmcBuffer;

    private final List<IEnergyStorage> validOutputs = new ArrayList<>(6);
    private boolean neverUpdatedNeighbours = true;

    int timer = 0;
    int totalFrames;
    long totalNanoseconds;

    public static float getEfficiencyRaw(int num)
    {
        switch(num)
        {
            case 0: return 0.5f;
            case 1: return 1f;
            case 2: return 2f;
            case 3: return 3f;
            default: return 4f;
        }
    }

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

    public int getPowerPerTick()
    {
        return powerPerTick;
    }

    public void setPowerPerTick(int ppt)
    {
        if(ppt > 0 && ppt < 50000)
        {
            this.powerPerTick = ppt;
            markDirty();
            notifyUpdate();
        }
    }

    public boolean isGenerating()
    {
        return generatedLastTick;
    }

    public boolean getGenerating() {
        return generating;
    }

    public void setGenerating(boolean generating)
    {
        this.generating = generating;
        markDirty();
        notifyUpdate();
    }

    @Override
    protected void onNewOwner(UUID oldOwner)
    {
        if(world == null || world.isRemote) return;

        //if(!generating) generatedLastTick = false;
        if(oldOwner != null && internalEmcBuffer > 0)
        {
            refundEmc(oldOwner);
        }
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

        double mult = ModConfig.generatorEMCMultiplier;
        if(mult <= 0)
        {
            mult = 0.01;
        }

        return (float)(getEfficiencyRaw(stack.getCount()) / mult);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey("generating"))
        {
            generating = compound.getBoolean("generating");
        }
        else
        {
            generating = false;
        }

        if(compound.hasKey("buffer"))
        {
            buffer = compound.getInteger("buffer");
        }
        else
        {
            buffer = 0;
        }

        if(compound.hasKey("powerPerTick"))
        {
            powerPerTick = compound.getInteger("powerPerTick");
        }
        else
        {
            powerPerTick = 10;
        }

        if(compound.hasKey("generatedLastTick"))
        {
            generatedLastTick = compound.getBoolean("generatedLastTick");
        }
        else
        {
            generatedLastTick = false;
        }

        if(compound.hasKey("internalEmc"))
        {
            internalEmcBuffer = compound.getDouble("internalEmc");
        }
        else
        {
            internalEmcBuffer = 0d;
        }

        markDirty();
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
        ret.setDouble("internalEmc", internalEmcBuffer);
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

        if(neverUpdatedNeighbours)
        {
            updateNeighbours();
            neverUpdatedNeighbours = false;
        }


        needsUpdate = pushEnergy();

        long startTime = System.nanoTime();
        needsUpdate = generateEnergy() || needsUpdate;
        long delta = System.nanoTime() - startTime;

        if(needsUpdate)
        {
            EquivalentIntegrationsMod.logger.info("Requesting update for Generator");
            notifyUpdate();
        }



        totalNanoseconds += delta;
        totalFrames += 1;

        timer++;

        if(timer % 35 == 0)
        {
            EquivalentIntegrationsMod.logger.info("Avg update microsecond: {}", (totalNanoseconds / totalFrames) / 1000);
            totalFrames = 0;
            totalNanoseconds = 0;
        }


    }

    public void updateNeighbours()
    {
        determineOutputs();
    }

    private void determineOutputs()
    {
        validOutputs.clear();

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

            validOutputs.add(energyStorage);
        }

    }

    private boolean pushEnergy()
    {
        if(buffer <= 0)
            return false;

        List<IEnergyStorage> toSend = validOutputs;
        //for(IEnergyStorage energyStorage : validOutputs)
        //{
        //    if(energyStorage.canReceive()) toSend.add(energyStorage);
        //}

        boolean isDirty = false;

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

                if(sent > 0) isDirty = true;

                buffer -= sent;
            }
        }

        if(isDirty) markDirty();

        return false;
    }

    private boolean generateEnergy()
    {
        boolean prevGenerating = isGenerating();

        generatedLastTick = false;

        if(generating)
        {
            float energyModifier = getEfficiency();

            int energyToGet = getMaxEnergyStored() - buffer;
            if (energyToGet > powerPerTick)
            {
                energyToGet = powerPerTick;
            }

            double emcToConsume = energyToGet / energyModifier;

            if(internalEmcBuffer < emcToConsume)
            {
                fillEmcBuffer();
            }

            if(internalEmcBuffer < emcToConsume)
            {
                //we can't use it, so give it back
                //refundEmc();
            }
            else
            {
                internalEmcBuffer -= emcToConsume;
                buffer += energyToGet;
                generatedLastTick = true;
                markDirty();
            }
        }
        else if(owner != null && internalEmcBuffer > 0)
        {
            refundEmc(owner);
        }

        return isGenerating() != prevGenerating;
    }

    private void refundEmc(UUID owner)
    {
        if(EquivalentIntegrationsMod.emcManager != null)
        {
            EquivalentIntegrationsMod.emcManager.depositEMC(owner, (long)internalEmcBuffer);
            internalEmcBuffer = 0;
        }
    }

    private void fillEmcBuffer()
    {
        if(owner == null) return;

        float energyModifier = getEfficiency();
        long emcToGet = (long)Math.ceil(powerPerTick * 20 / energyModifier);

        emcToGet = EquivalentIntegrationsMod.emcManager.withdrawEMC(owner, emcToGet);

        internalEmcBuffer += emcToGet;
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
            markDirty();
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

    public float getEMCPerTick()
    {
        return Math.round(powerPerTick / getEfficiency() * 100) / 100f;
    }
}
