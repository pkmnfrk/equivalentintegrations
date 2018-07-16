package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.TransmutationTileEntityBase;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
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

    private int ticksSinceLastUpdate = 0;

    @Nonnull
    @Override
    protected ItemStackHandler createInventory()
    {
        return new TransmutationChamberItemStackHandler()
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
        if(world.isRemote) return;

        if (owner != null)
        {
            createEmcItemHandler(owner);
            setTransmutationParameters();
        }
    }

    private void createEmcItemHandler(UUID newOwner)
    {
        emcItemHandler = new EMCItemHandler(newOwner, world);
        MinecraftForge.EVENT_BUS.register(emcItemHandler);
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

    private boolean getCanLearn()
    {
        ItemStack stack = inventory.getStackInSlot(1);

        return !stack.isEmpty();
    }

    private void setTransmutationParameters()
    {
        if(emcItemHandler == null) return;

        emcItemHandler.setCanLearn(getCanLearn());
        emcItemHandler.setEfficiencyThreshold(getEfficiencyThreshold());
    }

    private void destroyEmcItemHandler()
    {
        MinecraftForge.EVENT_BUS.unregister(emcItemHandler);
        emcItemHandler = null;
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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null)
        {
            //if the facing has an actual value, assume they mean the EMC inventory
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
            createEmcItemHandler(owner);
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
}
