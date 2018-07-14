package com.mike_caron.equivalentintegrations;

import com.mike_caron.equivalentintegrations.impl.EMCManagerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.mike_caron.equivalentintegrations.proxy.CommonProxy;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(
        modid = EquivalentIntegrationsMod.modId,
        name = EquivalentIntegrationsMod.name,
        version = EquivalentIntegrationsMod.version,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:projecte"
)
@Mod.EventBusSubscriber
public class EquivalentIntegrationsMod {
    public static final String modId = "equivalentintegrations";
    public static final String name = "Equivalent Integrations";
    public static final String version = "0.1.0";

    public static final Logger logger = LogManager.getLogger(modId);

    @SuppressWarnings("unused")
    @Mod.Instance(modId)
    public static EquivalentIntegrationsMod instance;

    @SidedProxy(
            serverSide = "com.mike_caron.equivalentintegrations.proxy.CommonProxy",
            clientSide = "com.mike_caron.equivalentintegrations.proxy.ClientProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public  void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        proxy.onPlayerLogin(event);
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event)
    {
        event.addCapability(new ResourceLocation(modId, "emcManager"), new EMCManagerProvider());
    }

}
