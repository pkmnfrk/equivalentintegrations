package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.BetterItemStackHandler;
import com.mike_caron.equivalentintegrations.item.ModItems;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class TransmutationChamberItemStackHandler extends BetterItemStackHandler
{
    public static final int NUM_SLOTS = 3;

    public TransmutationChamberItemStackHandler()
    {
        super(NUM_SLOTS);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        setSize(NUM_SLOTS);
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
        else if(slot == 2 && stack.getItem() == ModItems.efficiencyCatalyst)
        {
            return 4;
        }

        return 0;
    }
}
