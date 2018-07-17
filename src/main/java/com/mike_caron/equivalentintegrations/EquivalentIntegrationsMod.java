package com.mike_caron.equivalentintegrations;

import com.mike_caron.equivalentintegrations.integrations.MainCompatHandler;
import com.mike_caron.equivalentintegrations.network.CtoSMessage;
import com.mike_caron.equivalentintegrations.network.PacketHandlerServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.mike_caron.equivalentintegrations.proxy.CommonProxy;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
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
    public static final String version = "0.1.2";

    public static final Logger logger = LogManager.getLogger(modId);

    public static final CreativeTab creativeTab = new CreativeTab();

    @SuppressWarnings("unused")
    @Mod.Instance(modId)
    public static EquivalentIntegrationsMod instance;

    @SidedProxy(
            serverSide = "com.mike_caron.equivalentintegrations.proxy.CommonProxy",
            clientSide = "com.mike_caron.equivalentintegrations.proxy.ClientProxy"
    )
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper networkWrapper;

    @Mod.EventHandler
    public  void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);

        MainCompatHandler.registerAll();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modId);
        networkWrapper.registerMessage(PacketHandlerServer.class, CtoSMessage.class, 2, Side.SERVER);
    }

}
