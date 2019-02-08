package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.util.MappedModelLoader;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder(EquivalentIntegrationsMod.modId)
public class ModItems
{
    @GameRegistry.ObjectHolder(SoulboundTalisman.id)
    public static SoulboundTalisman soulboundTalisman;

    @GameRegistry.ObjectHolder(AlchemicalAlgorithms.id)
    public static AlchemicalAlgorithms alchemicalAlgorithms;

    @GameRegistry.ObjectHolder(EfficiencyCatalyst.id)
    public static EfficiencyCatalyst efficiencyCatalyst;

    @GameRegistry.ObjectHolder(ConjurationAssembler.id)
    public static ConjurationAssembler conjurationAssembler;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(new SoulboundTalisman());
        registry.register(new AlchemicalAlgorithms());
        registry.register(new EfficiencyCatalyst());
        registry.register(new ConjurationAssembler());
    }

    public static void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(conjurationAssembler);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        MappedModelLoader.Builder models = MappedModelLoader.builder();
        try
        {
            for (Field field : ModItems.class.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) && ItemBase.class.isAssignableFrom(field.getType()))
                {
                    ItemBase item = (ItemBase) field.get(null);

                    item.initModel(models);
                }
            }
        }
        catch(IllegalAccessException ex)
        {
            throw new RuntimeException("Unable to reflect upon myelf??");
        }
        //soulboundTalisman.initModel();

        ModelLoaderRegistry.registerLoader(models.build(EquivalentIntegrationsMod.modId));
    }
}
