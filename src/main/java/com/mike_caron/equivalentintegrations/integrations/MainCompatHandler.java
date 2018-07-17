package com.mike_caron.equivalentintegrations.integrations;

import net.minecraftforge.fml.common.Loader;

public class MainCompatHandler
{
    public static void registerAll()
    {
        registerTOP();
    }

    public static void registerTOP()
    {
        if(Loader.isModLoaded("theoneprobe"))
        {
            TOPCompatibility.register();
        }
    }
}
