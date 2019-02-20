package com.mike_caron.equivalentintegrations.block;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamber;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.equivalentintegrations.block.transmutation_generator.TransmutationGenerator;
import com.mike_caron.equivalentintegrations.block.transmutation_generator.TransmutationGeneratorTileEntity;
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
import com.mike_caron.mikesmodslib.block.BlockBase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder (EquivalentIntegrationsMod.modId)
public class ModBlocks
{
    @GameRegistry.ObjectHolder(TransmutationChamber.ID_TRANSMUTATION_CHAMBER)
    public static TransmutationChamber transmutationChamber;
    @GameRegistry.ObjectHolder(TransmutationChamber.ID_TRANSMUTATION_DISASSEMBLER)
    public static TransmutationChamber transmutationDisassembler;

    @GameRegistry.ObjectHolder(TransmutationGenerator.id)
    public static TransmutationGenerator transmutationGenerator;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new TransmutationChamber(TransmutationChamber.ID_TRANSMUTATION_CHAMBER));
        registry.register(new TransmutationChamber(TransmutationChamber.ID_TRANSMUTATION_DISASSEMBLER));
        registry.register(new TransmutationGenerator());

        GameRegistry.registerTileEntity(TransmutationChamberTileEntity.class, new ResourceLocation(EquivalentIntegrationsMod.modId, TransmutationChamber.ID_TRANSMUTATION_CHAMBER));
        GameRegistry.registerTileEntity(TransmutationGeneratorTileEntity.class, new ResourceLocation(EquivalentIntegrationsMod.modId, TransmutationGenerator.id));
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        try
        {
            for (Field field : ModBlocks.class.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) && BlockBase.class.isAssignableFrom(field.getType()))
                {
                    BlockBase block = (BlockBase) field.get(null);

                    registry.register(
                            new ItemBlock(block)
                            .setRegistryName(block.getRegistryName())
                    );
                }
            }
        }
        catch(IllegalAccessException ex)
        {
            throw new RuntimeException("Unable to reflect upon myelf??");
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        try
        {
            for (Field field : ModBlocks.class.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) && BlockBase.class.isAssignableFrom(field.getType()))
                {
                    BlockBase block = (BlockBase) field.get(null);

                    block.initModel();
                }
            }
        }
        catch(IllegalAccessException ex)
        {
            throw new RuntimeException("Unable to reflect upon myelf??");
        }
    }
}
