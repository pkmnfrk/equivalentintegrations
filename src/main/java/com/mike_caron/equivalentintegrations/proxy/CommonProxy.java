package com.mike_caron.equivalentintegrations.proxy;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {

    public void init(FMLInitializationEvent e)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(EquivalentIntegrationsMod.instance, new GuiProxy());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {

    }
}
