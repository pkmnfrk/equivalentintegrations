package com.mike_caron.equivalentintegrations;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = EquivalentIntegrationsMod.modId)
@Config.LangKey("equivalentintegrations.config.title")
public class ModConfig
{
    private static final String CATEGORY_GENERAL = "general";

    @Config.Comment("The amount to multiply EMC consumption by in the Transmutation Generator. Does not, directly, affect energy production")
    @Config.RangeDouble(min = 0.01, max = 100.0)
    @Config.LangKey("equivalentintegrations.config.generatoremcmultiplier")
    public static double generatorEMCMultiplier = 1.0;

    @Config.Comment("The maximum amount of a single item to expose, even if you have enough EMC to make more.")
    @Config.RangeInt(min = 64)
    @Config.LangKey("equivalentintegrations.config.maximumexposedstacksize")
    public static int maximumExposedStackSize = 1000000;

    @Mod.EventBusSubscriber(modid = EquivalentIntegrationsMod.modId)
    private static class EventHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if(event.getModID().equals(EquivalentIntegrationsMod.modId))
            {

                ConfigManager.sync(EquivalentIntegrationsMod.modId, Config.Type.INSTANCE);
            }
        }
    }
}
