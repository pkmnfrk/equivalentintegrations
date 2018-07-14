package com.mike_caron.equivalentintegrations.impl;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.OfflineEMCWorldData;
import com.mike_caron.equivalentintegrations.api.capabilities.IEMCManager;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ManagedEMCManager implements IEMCManager
{
    World world;

    public ManagedEMCManager(World world)
    {
        this.world = world;
    }

    @Override
    public double getEMC(UUID owner)
    {
        EntityPlayerMP player = getEntityPlayerMP(owner);

        if(player == null && OfflineEMCWorldData.get(world).hasCachedEMC(owner))
        {
            EquivalentIntegrationsMod.logger.debug("Retrieving cached EMC value for {}", owner);
            return OfflineEMCWorldData.get(world).getCachedEMC(owner);
        }

        EquivalentIntegrationsMod.logger.debug("Retrieving live EMC value for {}", owner);
        IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        return knowledge.getEmc();
    }

    @Override
    public void setEMC(UUID owner, double emc)
    {
        EntityPlayerMP player = getEntityPlayerMP(owner);

        if (player != null)
        {
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
            knowledge.setEmc(emc);
            knowledge.sync(player);
        }
        else
        {
            OfflineEMCWorldData.get(world).setCachedEMC(owner, emc);
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

    public static class Factory implements Callable<IEMCManager>
    {

        @Override
        public IEMCManager call() throws Exception
        {
            return new ManagedEMCManager(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
        }
    }
}
