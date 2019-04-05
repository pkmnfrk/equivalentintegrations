package com.mike_caron.equivalentintegrations.storage;

import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class SlotlessEMCItemHandler
    implements ISlotlessItemHandler
{
    private EMCItemHandler parent;

    public SlotlessEMCItemHandler(EMCItemHandler emcItemHandler)
    {
        this.parent = emcItemHandler;
    }


    @Override
    public Iterator<ItemStack> getItems()
    {
        return parent.getCachedInventory().iterator();
    }

    @Override
    public Iterator<ItemStack> findItems(@Nonnull ItemStack itemStack, int matchFlags)
    {
        return parent.getCachedInventory().stream().filter(is -> ItemMatch.areItemStacksEqual(itemStack, is, matchFlags)).iterator();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack itemStack, boolean simulate)
    {
        return parent.insertItem(0, itemStack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int amount, boolean simulate)
    {
        return parent.extractItem(0, amount, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(@Nonnull ItemStack itemStack, int matchFlags, boolean simulate)
    {
        if(ItemMatch.EXACT == (matchFlags & ItemMatch.EXACT))
        {
            return parent.extractItem(itemStack, simulate);
        }

        ItemStack found = parent.getCachedInventory().stream().filter(is -> ItemMatch.areItemStacksEqual(itemStack, is, matchFlags)).findFirst().orElse(null);

        if(found == null) return ItemStack.EMPTY;

        return parent.extractItem(found, simulate);
    }

    @Override
    public int getLimit()
    {
        return Integer.MAX_VALUE;
    }
}
