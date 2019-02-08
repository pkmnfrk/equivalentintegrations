package com.mike_caron.equivalentintegrations.proxy;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.ModBlocks;
import com.mike_caron.equivalentintegrations.item.ModItems;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy
    extends CommonProxy
{

    @Override
    public void preInit(FMLPreInitializationEvent e)
    {
        super.preInit(e);

        OBJLoader.INSTANCE.addDomain(EquivalentIntegrationsMod.modId);

    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ModBlocks.initModels();
        ModItems.initModels();
    }
}
