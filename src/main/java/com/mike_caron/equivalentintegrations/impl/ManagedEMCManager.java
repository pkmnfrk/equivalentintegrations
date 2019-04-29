package com.mike_caron.equivalentintegrations.impl;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.OfflineEMCWorldData;
import com.mike_caron.equivalentintegrations.api.events.EMCChangedEvent;
import com.mike_caron.equivalentintegrations.integrations.projecte.ProjectEWrapper;
import com.mike_caron.equivalentintegrations.storage.EMCInventory;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.NBTWhitelist;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import scala.Tuple2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Mod.EventBusSubscriber
public class ManagedEMCManager
{
    private static int TICK_DELAY = 60;
    private static int EMC_CHECK_DELAY = 5;

    private World world;

    private final HashMap<UUID, EMCInventory> emcInventories = new HashMap<>();

    private HashMap<UUID, Integer> dirtyPlayers = new HashMap<>();
    private HashMap<UUID, Double> lastKnownEmc = new HashMap<>();
    private HashSet<UUID> updateEmc = new HashSet<>();

    private final HashMap<Tuple2<Item, Integer>, Long> emcValues = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    //private final HashMap<ItemStack, Boolean> cacheBlacklist = new HashMap<>();


    private int emcCheckTimer = 0;

    public ManagedEMCManager(World world)
    {
        this.world = world;
    }

    public double getEMC(@Nonnull World world, @Nonnull UUID owner)
    {
        double ret = -1D;

        if(world.isRemote)
        {
            return ProjectEWrapper.instance.getEmc(world, owner);
        }

        if(ProjectEWrapper.instance.isSafe())
        {
                ret = ProjectEWrapper.instance.getEmc(world, owner);
        }
        else
        {

            EntityPlayerMP player = getEntityPlayerMP(owner);

            if (player == null && OfflineEMCWorldData.get(world).hasCachedEMC(owner))
            {
                EquivalentIntegrationsMod.debugLog("Retrieving cached EMC value for {}", owner);
                ret = OfflineEMCWorldData.get(world).getCachedEMC(owner);
            }

            if (ret == -1D)
            {
                EquivalentIntegrationsMod.debugLog("Retrieving live EMC value for {}", owner);
                ret = ProjectEWrapper.instance.getEmc(world, owner);
            }

        }

        if(!lastKnownEmc.containsKey(owner))
        {
            lastKnownEmc.put(owner, 0D);
        }

        if(lastKnownEmc.get(owner) != ret)
        {
            lastKnownEmc.put(owner, ret);
            if(!ProjectEWrapper.instance.isSafe())
            {
                OfflineEMCWorldData.get(world).setCachedEMC(owner, ret);
            }
            //MinecraftForge.EVENT_BUS.post(new EMCChangedEvent(owner, ret));
            updateEmc.add(owner);
        }

        EquivalentIntegrationsMod.debugLog("Returned EMC value: {}", ret);

        return ret;
    }

    public void setEMC(@Nonnull World world, @Nonnull UUID owner, double emc)
    {
        if(world.isRemote)
            throw new IllegalStateException("Unable to modify EMC on client side");

        lock.lock();
        try
        {
            double currentEmc = getEMC(world, owner);
            if (emc != currentEmc)
            {
                if(ProjectEWrapper.instance.isSafe())
                {
                    ProjectEWrapper.instance.setEmc(world, owner, emc);
                    markDirty(owner);
                }
                else
                {
                    EntityPlayerMP player = getEntityPlayerMP(owner);

                    if (player != null)
                    {
                        ProjectEWrapper.instance.setEmc(world, owner, emc);
                        markDirty(owner);
                    }
                    else
                    {
                        OfflineEMCWorldData.get(world).setCachedEMC(owner, emc);
                    }
                }

                lastKnownEmc.put(owner, emc);
                //MinecraftForge.EVENT_BUS.post(new EMCChangedEvent(owner, emc));
                updateEmc.add(owner);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public long withdrawEMC(@Nonnull World world, @Nonnull UUID owner, long amt)
    {
        if(world.isRemote)
            throw new IllegalStateException("Unable to modify EMC on client side");

        lock.lock();
        try
        {
            double currentEmc = getEMC(world, owner);
            if (amt > currentEmc)
            {
                amt = (long) currentEmc;
            }

            double newEmc = currentEmc - amt;

            if (newEmc != currentEmc)
            {
                setEMC(world, owner, newEmc);
            }

            return amt;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void depositEMC(@Nonnull World world, @Nonnull UUID owner, long amt)
    {
        if(world.isRemote)
            throw new IllegalStateException("Unable to modify EMC on client side");

        lock.lock();
        try
        {
            double currentEmc = getEMC(world, owner);

            double newEmc = currentEmc + amt;

            if (newEmc != currentEmc)
            {
                setEMC(world, owner, newEmc);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void tick(@Nonnull World world)
    {
        if(world.isRemote)
            return;

        lock.lock();

        try
        {
            Iterator<UUID> keys = dirtyPlayers.keySet().iterator();
            while(keys.hasNext())
            {
                UUID player = keys.next();
                int ticks = dirtyPlayers.get(player);
                ticks--;

                if (ticks <= 0)
                {
                    keys.remove();

                    EntityPlayerMP playermp = getEntityPlayerMP(player);

                    if (playermp == null)
                    {
                        //they went offline... no problem
                    }
                    else
                    {
                        ProjectEWrapper.instance.sync(playermp);
                    }
                }
                else
                {
                    dirtyPlayers.put(player, ticks);
                }
            }

            if (--emcCheckTimer <= 0)
            {
                emcCheckTimer = EMC_CHECK_DELAY;

                for (UUID player : lastKnownEmc.keySet())
                {
                    getEMC(world, player); //the event will be fired from within
                }
            }

            for (UUID player : updateEmc)
            {
                double emc = lastKnownEmc.get(player);

                MinecraftForge.EVENT_BUS.post(new EMCChangedEvent(player, emc));
            }

            updateEmc.clear();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void playerLoggedIn(UUID owner)
    {
        if(world.isRemote)
            return;

        if(ProjectEWrapper.instance.isSafe())
            return;

        lock.lock();
        try
        {
            OfflineEMCWorldData data = OfflineEMCWorldData.get(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
            if (data.hasCachedEMC(owner))
            {
                ProjectEWrapper.instance.setEmc(world, owner, data.getCachedEMC(owner));

                EntityPlayerMP player = getEntityPlayerMP(owner);
                ProjectEWrapper.instance.sync(player);
            }
            //knowledgeProviders.remove(owner);
        }
        finally
        {
            lock.unlock();
        }
    }

    public long getEmcValue(ItemStack stack)
    {
        //return 100;
        //return EMCHelper.getEmcValue(stack);

        lock.lock();
        try
        {
            if(!stack.isItemStackDamageable() && !stack.hasTagCompound())
            {
                Tuple2<Item, Integer> key = new Tuple2<>(stack.getItem(), stack.getMetadata());
                if (!emcValues.containsKey(key))
                {
                    long value = EMCHelper.getEmcValue(stack);
                    emcValues.put(key, value);
                }
                return emcValues.get(key);
            }
            else
            {
                return EMCHelper.getEmcValue(stack);
            }
        }
        finally
        {
            lock.unlock();
        }

    }

    public long getEmcSellValue(ItemStack stack)
    {
        return EMCHelper.getEmcSellValue(stack);
        /*ItemStack idealStack = getIdeal(stack);
        lock.lock();

        try
        {
            if(!cacheBlacklist.containsKey(idealStack))
            {
                cacheBlacklist.put(idealStack, idealStack.getMaxDamage() > 0);
            }

            if(cacheBlacklist.get(idealStack))
            {
                return EMCHelper.getEmcSellValue(stack);
            }

            if (!emcValues.containsKey(idealStack))
            {
                long value = EMCHelper.getEmcValue(idealStack);
                emcValues.put(idealStack, value);
            }


            return (long) (emcValues.get(stack) * EMCMapper.covalenceLoss);
        }
        finally
        {
            lock.unlock();
        }*/
    }

    public EMCInventory getEMCInventory(@Nonnull World world, UUID owner)
    {
        lock.lock();
        try
        {
            if (!emcInventories.containsKey(owner))
            {
                EMCInventory inv = new EMCInventory(world, owner, this);
                MinecraftForge.EVENT_BUS.register(inv);
                emcInventories.put(owner, inv);
            }
            return emcInventories.get(owner);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void bustCache()
    {
        if(world.isRemote)
            return;

        lock.lock();
        try
        {
            //cacheBlacklist.clear();
            emcValues.clear();
        }
        finally
        {
            lock.unlock();
        }

    }

    private void markDirty(UUID owner)
    {
        if(world.isRemote)
            return;

        if(!dirtyPlayers.containsKey(owner))
        {
            dirtyPlayers.put(owner, TICK_DELAY);
        }
    }

    @Nullable
    private EntityPlayerMP getEntityPlayerMP(UUID owner)
    {
        if(world.isRemote)
            return null;

        EntityPlayerMP player = null;
        MinecraftServer server = world.getMinecraftServer();
        if (server != null)
        {
            player = server.getPlayerList().getPlayerByUUID(owner);
        }
        return player;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        tick(event.world);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        //if(world.isRemote)
        //    return;

        UUID owner = event.player.getUniqueID();

        playerLoggedIn(owner);
    }

    public void onEmcRemap(EMCRemapEvent event)
    {
        bustCache();
    }

    public boolean hasKnowledge(@Nonnull World world, @Nonnull UUID owner, @Nonnull ItemStack stack)
    {
        EMCInventory inv = getEMCInventory(world, owner);
        ItemStack ideal = getIdeal(stack);

        return inv.itemKnown(ideal);
    }

    public boolean tryLearn(@Nonnull World world, @Nonnull UUID owner, @Nonnull ItemStack stack, boolean simulate)
    {
        EMCInventory inv = getEMCInventory(world, owner);
        if(!simulate)
        {
            stack = getIdeal(stack);
        }

        EntityPlayer player = world.getPlayerEntityByUUID(owner);
        if(player != null && !MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, stack)))
        {
            return simulate || ProjectEWrapper.instance.addKnowledge(world, owner,stack);
        }

        return false;
    }



    public void unload()
    {
        lock.lock();
        try
        {
            for (EMCInventory inv : emcInventories.values())
            {
                MinecraftForge.EVENT_BUS.unregister(inv);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private ItemStack getIdeal(ItemStack stack)
    {
        ItemStack idealStack = stack.copy();

        if(idealStack.getCount() != 1)
            idealStack.setCount(1);

        if(ItemHelper.isDamageable(idealStack))
            idealStack.setItemDamage(0);

        if(!NBTWhitelist.shouldDupeWithNBT(idealStack))
            idealStack.setTagCompound(null);

        return idealStack;
    }

}
