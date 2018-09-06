package com.mike_caron.equivalentintegrations.integrations;

import com.mike_caron.equivalentintegrations.integrations.refinedstorage.RefinedStorageCompatibility;
import net.minecraftforge.fml.common.Loader;

public class MainCompatHandler
{
    public static void registerAll()
    {
        registerTOP();
        registerWaila();

    }

    public static void registerAllLate()
    {
        registerRefinedStorage();
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

    public static void registerRefinedStorage()
    {
        if(Loader.isModLoaded("refinedstorage"))
        {
            RefinedStorageCompatibility.register();
        }
    }
}
