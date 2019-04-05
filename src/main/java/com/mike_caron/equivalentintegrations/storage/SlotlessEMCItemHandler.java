package com.mike_caron.equivalentintegrations.storage;

import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.mikesmodslib.util.Collect;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class SlotlessEMCItemHandler
    implements ISlotlessItemHandler
{
    private TransmutationChamberTileEntity parent;
    private static final Collection<ItemStack> empty = new HashSet<>();

    public SlotlessEMCItemHandler(TransmutationChamberTileEntity parent)
    {
        this.parent = parent;
    }


    @Override
    public Iterator<ItemStack> getItems()
    {
        EMCItemHandler handler = getItemHandler();
        if(handler == null)
            return empty.iterator();

        return handler.getCachedInventory().iterator();
    }

    @Override
    public Iterator<ItemStack> findItems(@Nonnull ItemStack itemStack, int matchFlags)
    {
        EMCItemHandler handler = getItemHandler();
        if(handler == null)
            return empty.iterator();

        return handler.getCachedInventory().stream().filter(is -> ItemMatch.areItemStacksEqual(itemStack, is, matchFlags)).iterator();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack itemStack, boolean simulate)
    {
        EMCItemHandler handler = getItemHandler();
        if(handler == null)
            return itemStack;

        return handler.insertItem(0, itemStack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int amount, boolean simulate)
    {
        EMCItemHandler handler = getItemHandler();
        if(handler == null)
            return ItemStack.EMPTY;

        return handler.extractItem(0, amount, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(@Nonnull ItemStack itemStack, int matchFlags, boolean simulate)
    {
        EMCItemHandler handler = getItemHandler();
        if(handler == null)
            return ItemStack.EMPTY;

        if(ItemMatch.EXACT == (matchFlags & ItemMatch.EXACT))
        {
            return handler.extractItem(itemStack, simulate);
        }

        ItemStack found = handler.getCachedInventory().stream().filter(is -> ItemMatch.areItemStacksEqual(itemStack, is, matchFlags)).findFirst().orElse(null);

        if(found == null) return ItemStack.EMPTY;

        return handler.extractItem(found, simulate);
    }

    @Override
    public int getLimit()
    {
        EMCItemHandler handler = getItemHandler();
        if(handler == null)
            return 0;

        return Integer.MAX_VALUE;
    }

    private EMCItemHandler getItemHandler()
    {
        return parent.getEmcItemHandler();
    }
}
