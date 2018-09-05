package com.mike_caron.equivalentintegrations.storage;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class EmptyItemHandler implements IItemHandler, IEMCInventory
{
    @Override
    public int getSlots()
    {
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 0;
    }

    @Override
    public Collection<ItemStack> getCachedInventory()
    {
        return Collections.emptySet();
    }
}
