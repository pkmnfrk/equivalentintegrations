package com.mike_caron.equivalentintegrations.proxy;

import com.mike_caron.equivalentintegrations.OfflineEMCWorldData;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.UUID;

public class ServerProxy extends CommonProxy
{
    @Override
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        UUID owner = event.player.getUniqueID();
        OfflineEMCWorldData data = OfflineEMCWorldData.get(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
        if(data.getCachedEMC(owner) != 0D)
        {
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
            knowledge.setEmc(data.getCachedEMC(owner));
            data.clearCachedEMC(owner);

            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(owner);
            knowledge.sync(player);
        }
    }
}
