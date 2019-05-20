package com.mike_caron.equivalentintegrations;

import com.mike_caron.equivalentintegrations.command.CleanupCommand;
import com.mike_caron.equivalentintegrations.command.DumpCacheCommand;
import com.mike_caron.equivalentintegrations.impl.ManagedEMCManager;
import com.mike_caron.equivalentintegrations.integrations.MainCompatHandler;
import com.mike_caron.equivalentintegrations.network.CtoSMessage;
import com.mike_caron.equivalentintegrations.network.ItemConfigMessage;
import com.mike_caron.equivalentintegrations.network.PacketHandlerServer;
import com.mike_caron.equivalentintegrations.proxy.IModProxy;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
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
        dependencies = "required-after:projecte@[1.12.2-PE1.4.1,)" +
            ";required-after:mikesmodslib@[1.0.2,)" +
            ";after:projectex@[1.1.0,)"
)
@Mod.EventBusSubscriber
public class EquivalentIntegrationsMod {
    public static final String modId = "equivalentintegrations";
    public static final String name = "Equivalent Integrations";
    public static final String version = "0.4.6";

    public static final Logger logger = LogManager.getLogger(modId);

    public static final CreativeTab creativeTab = new CreativeTab();

    @SuppressWarnings("unused")
    @Mod.Instance(modId)
    public static EquivalentIntegrationsMod instance;

    @SidedProxy(
            serverSide = "com.mike_caron.equivalentintegrations.proxy.CommonProxy",
            clientSide = "com.mike_caron.equivalentintegrations.proxy.ClientProxy"
    )
    public static IModProxy proxy;

    public static SimpleNetworkWrapper networkWrapper;

    private static ManagedEMCManager emcManager;

    public static ManagedEMCManager getEmcManager(World world)
    {
        if(emcManager == null)
        {
            emcManager = new ManagedEMCManager(world);
            MinecraftForge.EVENT_BUS.register(emcManager);
            logger.info("Created EMC Manager");
        }
        return emcManager;
    }

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
        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modId.substring(0, 20));
        networkWrapper.registerMessage(PacketHandlerServer.class, CtoSMessage.class, 2, Side.SERVER);
        networkWrapper.registerMessage(ItemConfigMessage.Handler.class, ItemConfigMessage.class, 3, Side.SERVER);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent evt)
    {
        evt.registerServerCommand(new CleanupCommand());
        evt.registerServerCommand(new DumpCacheCommand());
        emcManager = new ManagedEMCManager(evt.getServer().getEntityWorld());
        MinecraftForge.EVENT_BUS.register(emcManager);
        logger.info("Created EMC Manager");
    }

    @Mod.EventHandler
    public void serverUnload(FMLServerStoppingEvent evt)
    {
        MinecraftForge.EVENT_BUS.unregister(emcManager);
        emcManager.unload();
        emcManager = null;
    }

    public static void debugLog(String message, Object... params)
    {
        logger.debug(message, params);
    }

}
