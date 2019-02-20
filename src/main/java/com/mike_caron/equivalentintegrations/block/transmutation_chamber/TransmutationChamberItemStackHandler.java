package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.inventory.BetterItemStackHandler;
import com.mike_caron.equivalentintegrations.item.ModItems;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TransmutationChamberItemStackHandler extends BetterItemStackHandler
{
    private boolean hasEfficiency;

    public TransmutationChamberItemStackHandler(boolean hasEfficiency)
    {
        super(hasEfficiency ? 3 : 2);
        this.hasEfficiency = hasEfficiency;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack)
    {
        if(slot == 0 && stack.getItem() == ModItems.soulboundTalisman && SoulboundTalisman.isBound(stack))
        {
            return 1;
        }
        else if(slot == 1 && stack.getItem() == ModItems.alchemicalAlgorithms)
        {
            return 1;
        }
        else if(hasEfficiency && slot == 2 && stack.getItem() == ModItems.efficiencyCatalyst)
        {
            return 4;
        }

        return 0;
    }
}
