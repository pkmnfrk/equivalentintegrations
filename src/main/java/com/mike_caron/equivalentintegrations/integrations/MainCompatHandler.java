package com.mike_caron.equivalentintegrations.integrations;

import net.minecraftforge.fml.common.Loader;

public class MainCompatHandler
{
    public static void registerAll()
    {
        registerTOP();
        registerWaila();
    }

    public static void registerTOP()
    {
        if(Loader.isModLoaded("theoneprobe"))
        {
            TOPCompatibility.register();
        }
    }

    public static void registerWaila()
    {
        if(Loader.isModLoaded("waila"))
        {
            WailaCompatibility.register();
        }
    }
}
