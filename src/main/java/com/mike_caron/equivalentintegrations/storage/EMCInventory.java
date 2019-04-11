package com.mike_caron.equivalentintegrations.storage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.ModConfig;
import com.mike_caron.equivalentintegrations.api.events.EMCChangedEvent;
import com.mike_caron.equivalentintegrations.impl.ManagedEMCManager;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EMCInventory
{
    private final UUID owner;
    private final ManagedEMCManager emcManager;
    private final World world;
    //private final IEMCProxy emcProxy;

    //private double cachedEmc;

    @Nullable
    private ItemStack filter = null;

    private boolean needsRefresh = false;

    private List<ItemStack> cachedInventory = null;
    private List<ItemStack> cachedKnowledge = null;

    public EMCInventory(@Nonnull World world, UUID owner, ManagedEMCManager manager)
    {
        this(world, owner, manager, null);
    }

    private EMCInventory(@Nonnull World world, UUID owner, ManagedEMCManager manager, @Nullable ItemStack filter)
    {
        this.owner = owner;
        this.emcManager = manager;
        //this.emcProxy = ProjectEAPI.getEMCProxy();
        this.filter = filter;
        this.world = world;

        refresh();
    }


    public int getSlots()
    {
        return cachedInventory.size() + 64;
    }

    public ItemStack getStackInSlot(int slot)
    {
        int size = cachedInventory.size();

        if (slot < 0 || slot >= size)
        {
            return ItemStack.EMPTY;
        }

        return cachedInventory.get(slot);
    }

    public int getSlotLimit(int slot)
    {
        int size = cachedInventory.size();

        if(slot < 0 || slot >= size)
            return 0;

        return Integer.MAX_VALUE;
    }

    public List<ItemStack> inventory()
    {
        return this.cachedInventory;
    }

    public int realSize()
    {
        return cachedInventory.size();
    }

    public boolean refresh()
    {
        boolean ret;

        try
        {
            List<ItemStack> newKnowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner).getKnowledge();
            if(filter != null)
            {
                cachedKnowledge = newKnowledge.stream().filter(is -> is.isItemEqualIgnoreDurability(filter)).collect(Collectors.toList());
            }
            else
            {
                cachedKnowledge = newKnowledge;
            }
            calculateInventory(world);
            ret = true;
        }
        catch(IllegalStateException ex)
        {
            EquivalentIntegrationsMod.logger.warn("Unable to refresh knowledge, due to something");
            ret = false;
            if(cachedKnowledge == null)
            {
                cachedKnowledge = new ArrayList<>();
            }
        }

        needsRefresh = !ret;
        return ret;
    }

    @SubscribeEvent
    public void onPlayerKnowledgeChange(PlayerKnowledgeChangeEvent event)
    {
        if(event.getPlayerUUID().equals(owner))
        {
            EquivalentIntegrationsMod.logger.info("Refreshing cached knowledge due to knowledge change");
            refresh();
        }
    }

    @SubscribeEvent
    public void onEMCRemap(EMCRemapEvent event)
    {
        EquivalentIntegrationsMod.logger.info("Refreshing cached knowledge due to global remap");
        refresh();
    }

    @SubscribeEvent
    public void onEmcChanged(EMCChangedEvent event)
    {
        //refresh(true);
        if(event.player.equals(owner))
        {
            needsRefresh = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event)
    {
        tick();
    }

    private void tick()
    {
        if(needsRefresh)
        {
            refresh();
            needsRefresh = false;
        }
    }

    private void calculateInventory(@Nonnull World world)
    {
        if(cachedInventory != null)
        {
            //EquivalentIntegrationsMod.logger.info("Clearing array of size {}", cachedInventory.size());
            //cachedInventory.clear();
        }
        else
        {
            cachedInventory = new ArrayList<>();
        }

        //cachedInventory = new ArrayList<>();

        double cachedEmc = emcManager.getEMC(world, owner);

        int ix = 0;
        int addedNew = 0;
        int overwrote = 0;
        int updated = 0;

        for(int jx = 0; jx < cachedKnowledge.size(); jx++)
        {
            ItemStack is = cachedKnowledge.get(jx);

            long value = emcManager.getEmcValue(is);
            if(value == 0)
            {
                continue;
            }

            int num = howManyCanWeMake(cachedEmc, value);

            if(num > 0)
            {
                if (ix < cachedInventory.size())
                {
                    ItemStack cached = cachedInventory.get(ix);

                    if (cached.isItemEqualIgnoreDurability(is))
                    {
                        cached.setCount(num);
                        updated ++;
                    }
                    else
                    {
                        cachedInventory.set(ix, new ItemStack(is.getItem(), num, is.getMetadata(), is.getTagCompound()));
                        overwrote ++;
                    }
                }
                else
                {
                    cachedInventory.add(new ItemStack(is.getItem(), num, is.getMetadata(), is.getTagCompound()));
                    addedNew ++;
                }

                ix += 1;
            }
        }

        //EquivalentIntegrationsMod.logger.info("Refreshed knowledge. Updated {}, Overwrote {}, Added {}, Trimmed {}", updated, overwrote, addedNew, cachedInventory.size() - ix);

        while(cachedInventory.size() > ix)
        {
            cachedInventory.remove(ix);
        }
    }

    private static int howManyCanWeMake(double emc, long cost)
    {
        int cap = ModConfig.maximumExposedStackSize;

        if(cost == 0) return 0;

        double raw = emc / cost;
        long tmp = (long)Math.floor(raw);
        if (tmp > cap)
        {
            // er, let's cap it at that, shall we?
            return cap;
        }
        else
        {
            return (int) tmp;
        }
    }

    public Collection<ItemStack> getCachedInventory()
    {
        return cachedInventory;
    }

    public EMCInventory withFilter(ItemStack filter)
    {
        return new EMCInventory(world, owner, emcManager, filter);
    }
}
