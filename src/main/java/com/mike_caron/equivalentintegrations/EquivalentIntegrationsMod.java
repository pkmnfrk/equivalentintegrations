package com.mike_caron.equivalentintegrations;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.mike_caron.equivalentintegrations.proxy.CommonProxy;

@Mod(
        modid = EquivalentIntegrationsMod.modId,
        name = EquivalentIntegrationsMod.name,
        version = EquivalentIntegrationsMod.version,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:projecte"
)
public class EquivalentIntegrationsMod {
    public static final String modId = "equivalentintegrations";
    public static final String name = "Equivalent Integrations";
    public static final String version = "0.1.0";

    @Mod.Instance(modId)
    public static EquivalentIntegrationsMod instance;

    @SidedProxy(
            serverSide = "com.mike_caron.equivalentintegrations.proxy.CommonProxy",
            clientSide = "com.mike_caron.equivalentintegrations.proxy.ClientProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public  void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

}
