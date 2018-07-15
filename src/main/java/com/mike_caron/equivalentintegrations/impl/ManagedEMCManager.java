package com.mike_caron.equivalentintegrations.impl;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.OfflineEMCWorldData;
import com.mike_caron.equivalentintegrations.api.capabilities.IEMCManager;
import com.mike_caron.equivalentintegrations.api.events.EMCChangedEvent;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

@Mod.EventBusSubscriber
public class ManagedEMCManager implements IEMCManager
{
    private static int TICK_DELAY = 60;
    private static int EMC_CHECK_DELAY = 5;

    private World world;

    private HashMap<UUID, Integer> dirtyPlayers = new HashMap<>();
    private HashMap<UUID, Double> lastKnownEmc = new HashMap<>();

    private int emcCheckTimer = 0;

    public ManagedEMCManager(World world)
    {
        this.world = world;
    }

    @Override
    public double getEMC(UUID owner)
    {
        double ret = -1D;

        EntityPlayerMP player = getEntityPlayerMP(owner);

        if(player == null && OfflineEMCWorldData.get(world).hasCachedEMC(owner))
        {
            EquivalentIntegrationsMod.logger.debug("Retrieving cached EMC value for {}", owner);
            ret = OfflineEMCWorldData.get(world).getCachedEMC(owner);
        }

        if(ret == -1D)
        {
            EquivalentIntegrationsMod.logger.debug("Retrieving live EMC value for {}", owner);
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
            ret = knowledge.getEmc();
        }

        if(!lastKnownEmc.containsKey(owner))
        {
            lastKnownEmc.put(owner, 0D);
        }

        if(lastKnownEmc.get(owner) != ret)
        {
            lastKnownEmc.put(owner, ret);
            MinecraftForge.EVENT_BUS.post(new EMCChangedEvent(owner, ret));
        }

        return ret;
    }

    @Override
    public void setEMC(UUID owner, double emc)
    {
        double currentEmc = getEMC(owner);
        if(emc != currentEmc)
        {
            EntityPlayerMP player = getEntityPlayerMP(owner);

            if (player != null)
            {
                IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
                knowledge.setEmc(emc);
                markDirty(owner);
            }
            else
            {
                OfflineEMCWorldData.get(world).setCachedEMC(owner, emc);
            }

            lastKnownEmc.put(owner, emc);
            MinecraftForge.EVENT_BUS.post(new EMCChangedEvent(owner, emc));
        }
    }

    public void tick()
    {
        Set<UUID> keys = dirtyPlayers.keySet();
        for(UUID player : keys)
        {
            int ticks = dirtyPlayers.get(player);
            ticks--;

            if (ticks <= 0)
            {
                dirtyPlayers.remove(player);

                EntityPlayerMP playermp = getEntityPlayerMP(player);

                if (playermp == null)
                {
                    //they went offline... no problem
                }
                else
                {
                    IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(player);
                    knowledge.sync(playermp);
                }
            }
            else
            {
                dirtyPlayers.put(player, ticks);
            }
        }

        if(--emcCheckTimer <= 0)
        {
            emcCheckTimer = EMC_CHECK_DELAY;

            for(UUID player : lastKnownEmc.keySet())
            {
                getEMC(player); //the event will be fired from within
            }
        }
    }

    @Override
    public void playerLoggedIn(UUID owner)
    {
        OfflineEMCWorldData data = OfflineEMCWorldData.get(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
        if(data.hasCachedEMC(owner))
        {
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
            knowledge.setEmc(data.getCachedEMC(owner));
            data.clearCachedEMC(owner);

            EntityPlayerMP player = getEntityPlayerMP(owner);
            knowledge.sync(player);
        }
    }

    private void markDirty(UUID owner)
    {
        if(!dirtyPlayers.containsKey(owner))
        {
            dirtyPlayers.put(owner, TICK_DELAY);
        }
    }

    @Nullable
    private EntityPlayerMP getEntityPlayerMP(UUID owner)
    {
        EntityPlayerMP player = null;
        MinecraftServer server = world.getMinecraftServer();
        if (server != null)
        {
            player = server.getPlayerList().getPlayerByUUID(owner);
        }
        return player;
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        IEMCManager manager = event.world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

        if(manager != null) {
            manager.tick();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        UUID owner = event.player.getUniqueID();
        IEMCManager manager = event.player.world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

        if(manager != null)
        {
            manager.playerLoggedIn(owner);
        }
    }

    public static class Factory implements Callable<IEMCManager>
    {

        @Override
        public IEMCManager call() throws Exception
        {
            return new ManagedEMCManager(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
        }
    }
}
