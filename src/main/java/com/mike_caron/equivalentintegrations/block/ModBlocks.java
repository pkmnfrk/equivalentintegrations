package com.mike_caron.equivalentintegrations.block;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamber;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder (EquivalentIntegrationsMod.modId)
public class ModBlocks
{
    @GameRegistry.ObjectHolder(TransmutationChamber.id)
    public static TransmutationChamber transmutationChamber;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new TransmutationChamber());

        GameRegistry.registerTileEntity(TransmutationChamberTileEntity.class, new ResourceLocation(EquivalentIntegrationsMod.modId, TransmutationChamber.id));
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(new ItemBlock(transmutationChamber).setRegistryName(transmutationChamber.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        transmutationChamber.initModel();
    }
}
