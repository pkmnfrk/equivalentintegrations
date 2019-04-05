package com.mike_caron.equivalentintegrations.integrations;

import com.mike_caron.equivalentintegrations.integrations.bbw.TabletContainerHandler;
import com.mike_caron.equivalentintegrations.integrations.comcap.CommonCapabilitiesHandler;
import net.minecraftforge.fml.common.Loader;

public class MainCompatHandler
{
    public static void registerAll()
    {
        registerBBW();
    }

    private static void registerComCap()
    {
        if(Loader.isModLoaded("commoncapabilities"))
        {
            CommonCapabilitiesHandler.register();
        }
    }

    private static void registerBBW()
    {
        if(Loader.isModLoaded("betterbuilderswands"))
        {
            TabletContainerHandler.register();
        }
    }
}
