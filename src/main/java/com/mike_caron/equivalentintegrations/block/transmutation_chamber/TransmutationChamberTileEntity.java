package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.TransmutationTileEntityBase;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import com.mike_caron.equivalentintegrations.storage.EmptyItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TransmutationChamberTileEntity
        extends TransmutationTileEntityBase
        implements ITickable
{
    private EMCItemHandler emcItemHandler;

    private int type = -1; //0 = normal, 1 = disassembler only

    private int ticksSinceLastUpdate = 0;

    private boolean forbidNbt = false;
    private boolean forbidDamaged = false;

    private static final EmptyItemHandler emptyInventory = new EmptyItemHandler();

    public TransmutationChamberTileEntity()
    {

    }

    public TransmutationChamberTileEntity(int type)
    {
        this.type = type;
    }

    @Nonnull
    @Override
    protected ItemStackHandler createInventory()
    {
        return new TransmutationChamberItemStackHandler(type == 0)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                if(world.isRemote)
                    return;

                ItemStack stack = this.getStackInSlot(0);
                UUID owner = SoulboundTalisman.getOwnerFromStack(stack);

                TransmutationChamberTileEntity.this.setOwner(owner);
                TransmutationChamberTileEntity.this.markDirty();

                TransmutationChamberTileEntity.this.setTransmutationParameters();
            }
        };
    }

    @Override
    protected void onNewOwner(UUID oldOwner)
    {
        if(world == null || world.isRemote) return;

        createEmcItemHandler(owner, world);
        setTransmutationParameters();
    }

    private void createEmcItemHandler(UUID newOwner, World world)
    {
        if(EquivalentIntegrationsMod.getEmcManager(world) == null) {
            //EquivalentIntegrationsMod.logger.error("EMCManager is null when trying to create item handler!");
            return;
        }

        if(emcItemHandler != null)
        {
            MinecraftForge.EVENT_BUS.unregister(emcItemHandler);
            emcItemHandler = null;
        }

        if(newOwner != null)
        {
            emcItemHandler = new EMCItemHandler(newOwner, world, true, type == 0);
            MinecraftForge.EVENT_BUS.register(emcItemHandler);
        }
    }

    private int getEfficiencyThreshold()
    {
        ItemStack stack = inventory.getStackInSlot(2);

        if(stack.getCount() > 3) return Integer.MAX_VALUE;

        int ret = (int)(10 * Math.pow(10, stack.getCount()));

        //EquivalentIntegrationsMod.logger.info("Returning efficiency of {} for {}", ret, stack);

        return ret;
    }

    public static int getEfficiencyThreshold(int count)
    {
        if(count > 3) return Integer.MAX_VALUE;

        return (int)(10 * Math.pow(10, count));
    }

    private EMCItemHandler.EnumLearning getCanLearn()
    {
        ItemStack stack = inventory.getStackInSlot(1);

        boolean canLearn = !stack.isEmpty();

        if(type == 0)
            return canLearn ? EMCItemHandler.EnumLearning.CAN : EMCItemHandler.EnumLearning.CANNOT;
        else if(type == 1)
            return canLearn ? EMCItemHandler.EnumLearning.CAN : EMCItemHandler.EnumLearning.SKIP;

        return EMCItemHandler.EnumLearning.CANNOT;
    }

    private void setTransmutationParameters()
    {
        if(emcItemHandler == null) return;

        emcItemHandler.setCanLearn(getCanLearn());
        emcItemHandler.setEfficiencyThreshold(getEfficiencyThreshold());
        emcItemHandler.setForbidDamaged(forbidDamaged);
        emcItemHandler.setForbidNbt(forbidNbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        //if the facing has an actual value, assume they mean the EMC inventory
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null)
        {
            //things are weird at world load, it takes a little bit for this to get created
            if(emcItemHandler == null)
            {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(emptyInventory);
            }

            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(emcItemHandler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update()
    {
        if(world.isRemote) return;

        if(emcItemHandler == null && owner != null)
        {
            //things can get weird right at world load, so this is a fallback just in case
            createEmcItemHandler(owner, world);
            notifyUpdate();
        }

        if(emcItemHandler != null)
        {
            ticksSinceLastUpdate += 1;
            if (ticksSinceLastUpdate >= 20)
            {
                setTransmutationParameters();
                ticksSinceLastUpdate = 0;
            }
        }
    }

    public boolean getForbidNbt()
    {
        return forbidNbt;
    }

    public boolean getForbidDamage()
    {
        return forbidDamaged;
    }

    public void setForbidNbt(boolean forbid)
    {
        forbidNbt = forbid;
        setTransmutationParameters();
        markDirty();
        this.notifyUpdate();
    }

    public void setForbidDamaged(boolean forbid)
    {
        forbidDamaged = forbid;
        setTransmutationParameters();
        markDirty();
        this.notifyUpdate();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey("type"))
        {
            type = compound.getInteger("type");
        }
        else
        {
            type = 0;
        }

        if(compound.hasKey("forbidNbt"))
        {
            forbidNbt = compound.getBoolean("forbidNbt");
        }
        else
        {
            forbidNbt = false;
        }

        if(compound.hasKey("forbidDamaged"))
        {
            forbidDamaged = compound.getBoolean("forbidDamaged");
        }
        else
        {
            forbidDamaged = false;
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound ret = super.writeToNBT(compound);

        ret.setInteger("type", type);
        ret.setBoolean("forbidDamaged", forbidDamaged);
        ret.setBoolean("forbidNbt", forbidNbt);

        return ret;
    }

    public int getType()
    {
        return type;
    }

    public EMCItemHandler getEmcItemHandler()
    {
        return emcItemHandler;
    }
}
