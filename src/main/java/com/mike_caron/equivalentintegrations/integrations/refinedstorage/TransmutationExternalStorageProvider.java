package com.mike_caron.equivalentintegrations.integrations.refinedstorage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.equivalentintegrations.storage.EMCInventory;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import com.mike_caron.equivalentintegrations.storage.IEMCInventory;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.api.storage.externalstorage.IExternalStorageContext;
import com.raoulvdberge.refinedstorage.api.storage.externalstorage.IExternalStorageProvider;
import com.raoulvdberge.refinedstorage.api.storage.externalstorage.IStorageExternal;
import com.raoulvdberge.refinedstorage.api.util.Action;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.Sys;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TransmutationExternalStorageProvider
    implements IExternalStorageProvider<ItemStack>
{
    Map<TileEntity, IStorageExternal<ItemStack>> providers = new HashMap<>();

    @Override
    public int getPriority()
    {
        return 10;
    }

    @Override
    public boolean canProvide(TileEntity tileEntity, EnumFacing enumFacing)
    {
        if(enumFacing == null) return false;

        return tileEntity instanceof TransmutationChamberTileEntity;
    }

    @Nonnull
    @Override
    public IStorageExternal<ItemStack> provide(IExternalStorageContext iExternalStorageContext, Supplier<TileEntity> supplier, EnumFacing enumFacing)
    {
        TileEntity te = supplier.get();
        if(!providers.containsKey(te))
        {
            providers.put(te, new Implementation(iExternalStorageContext, (TransmutationChamberTileEntity)te));
        }

        return providers.get(supplier.get());
    }

    class Implementation
        implements IStorageExternal<ItemStack>
    {
        private TransmutationChamberTileEntity te;
        private IExternalStorageContext context;
        private List<ItemStack> cache;

        private long updateTime, flushTime;
        private int updateCount, changedCount;

        public Implementation(IExternalStorageContext context, TransmutationChamberTileEntity te)
        {
            this.context = context;
            this.te = te;
        }

        private IItemHandler getHandler()
        {
            return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        }

        private IEMCInventory getInventory()
        {
            return (IEMCInventory)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        }

        @Override
        public void update(INetwork iNetwork)
        {
            if(cache == null)
            {
                cache = new ArrayList<>(getStacks());
                return;
            }

            long updateStart = System.nanoTime();

            List<ItemStack> newStacks = new ArrayList<>(getStacks());
            //List<ItemStack> newStacks = getStacks().stream().map(ItemStack::copy).collect(Collectors.toList());

            int changed = 0;

            for(int i = 0; i < newStacks.size(); i++)
            {
                ItemStack actual = newStacks.get(i);

                if(i >= cache.size())
                {
                    if(!actual.isEmpty())
                    {
                        iNetwork.getItemStorageCache().add(actual, actual.getCount(), false, true);
                        changed += 1;
                    }

                    continue;
                }

                ItemStack cached = cache.get(i);

                if(!cached.isEmpty() && actual.isEmpty())
                {
                    iNetwork.getItemStorageCache().remove(cached, cached.getCount(), true);
                    changed += 1;
                }
                else if(cached.isEmpty() && !actual.isEmpty())
                {
                    iNetwork.getItemStorageCache().add(actual, actual.getCount(), false, true);
                    changed += 1;
                }
                else //noinspection StatementWithEmptyBody
                    if (cached.isEmpty() && actual.isEmpty())
                {
                    // this space left blank
                }
                else if(!ItemStack.areItemsEqual(actual, cached))
                {
                    iNetwork.getItemStorageCache().remove(cached, cached.getCount(), true);
                    iNetwork.getItemStorageCache().add(actual, actual.getCount(), false, true);
                    changed += 2;
                }
                else if(cached.getCount() != actual.getCount())
                {
                    int delta = actual.getCount() - cached.getCount();

                    if(delta > 0)
                    {
                        iNetwork.getItemStorageCache().add(actual, delta, false, true);
                        changed += 1;
                    }
                    else
                    {
                        iNetwork.getItemStorageCache().remove(actual, -delta, true);
                        changed += 1;
                    }
                }
            }

            if(cache.size() > newStacks.size())
            {
                for (int i = newStacks.size(); i < cache.size(); ++i) {
                    if (!cache.get(i).isEmpty()) {
                        iNetwork.getItemStorageCache().remove(cache.get(i), cache.get(i).getCount(), true);
                        changed += 1;
                    }
                }
            }

            this.cache = newStacks;

            long updateEnd = System.nanoTime();

            iNetwork.getItemStorageCache().flush();

            long flushEnd = System.nanoTime();

            updateTime += (updateEnd - updateStart);
            flushTime += (flushEnd - updateEnd);
            changedCount += changed;

            updateCount += 1;

            if(updateCount >= 20)
            {
                updateCount = 0;
                EquivalentIntegrationsMod.logger.info("Update: {}, Flush: {}, Change Count: {}", updateTime / 20000, flushTime / 20000, changedCount);

                changedCount = 0;
                updateTime = 0;
                flushTime = 0;
            }

        }

        @Override
        public int getCapacity()
        {
            if(te.hasOwner())
            {
                return 1000000000;
            }
            return 0;
        }

        @Override
        public Collection<ItemStack> getStacks()
        {
            IEMCInventory itemHandler = getInventory();

            if(itemHandler != null)
            {
                return itemHandler.getCachedInventory();
            }

            return Collections.emptySet();
        }

        @Nullable
        @Override
        public ItemStack insert(@Nonnull ItemStack itemStack, int size, Action action)
        {
            IItemHandler itemHandler = getHandler();

            if(itemHandler != null)
            {
                if(itemStack.getCount() != size)
                {
                    itemStack = ItemHandlerHelper.copyStackWithSize(itemStack, size);
                }

                ItemStack ret = itemHandler.insertItem(0, itemStack, action == Action.SIMULATE);

                if(ret.isEmpty())
                {
                    return null;
                }
            }

            return itemStack;
        }

        @Nullable
        @Override
        public ItemStack extract(@Nonnull ItemStack itemStack, int size, int flags, Action action)
        {
            IItemHandler itemHandlerTmp = getHandler();
            EMCItemHandler itemHandler = null;

            if(itemHandlerTmp instanceof  EMCItemHandler)
            {
                itemHandler = (EMCItemHandler)itemHandlerTmp;
            }

            if(itemHandler != null)
            {
                if(itemStack.getCount() != size)
                {
                    itemStack = ItemHandlerHelper.copyStackWithSize(itemStack, size);
                }

                return itemHandler.extractItem(itemStack, action == Action.SIMULATE);
            }

            return itemStack;
        }

        @Override
        public int getStored()
        {
            int ret = 0;

            Collection<ItemStack> stacks = getStacks();

            for(ItemStack stack : stacks)
            {
                ret += stack.getCount();
                if(ret > 1000000000)
                {
                    return 1000000000;
                }
            }

            return ret;
        }

        @Override
        public int getPriority()
        {
            return context.getPriority();
        }

        @Override
        public AccessType getAccessType()
        {
            return context.getAccessType();
        }

        @Override
        public int getCacheDelta(int storedPre, int size, @Nullable ItemStack itemStack)
        {
            // I think this is correct???
            // honestly, the way it's called doesn't seem to make any sense.
            if(itemStack != null)
            {
                size -= itemStack.getCount();
            }
            return size;
        }
    }
}
