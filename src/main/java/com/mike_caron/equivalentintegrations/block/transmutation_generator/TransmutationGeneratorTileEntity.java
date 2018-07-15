package com.mike_caron.equivalentintegrations.block.transmutation_generator;

import com.mike_caron.equivalentintegrations.block.TransmutationTileEntityBase;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TransmutationGeneratorTileEntity
        extends TransmutationTileEntityBase
{
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

    @Override
    protected void onNewOwner()
    {
        //todo: something here, probably
    }

    public boolean isGenerating()
    {
        return false;
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
}
