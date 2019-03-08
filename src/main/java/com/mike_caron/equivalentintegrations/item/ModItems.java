package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.ModConfig;
import com.mike_caron.mikesmodslib.item.ItemBase;
import com.mike_caron.mikesmodslib.util.MappedModelLoader;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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

    public static TestItem[] testItems;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(new SoulboundTalisman());
        registry.register(new AlchemicalAlgorithms());
        registry.register(new EfficiencyCatalyst());
        registry.register(new ConjurationAssembler());

        addDummyItems(registry);
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

                    item.initModel();
                }

                if (Modifier.isStatic(field.getModifiers()) && field.getType().isArray() && ItemBase.class.isAssignableFrom(field.getType().getComponentType()))
                {
                    ItemBase[] items = (ItemBase[]) field.get(null);

                    if(items == null) continue;

                    for(ItemBase item : items)
                    {
                        if(item != null)
                        {
                            item.initModel();
                        }
                    }
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

    private static void addDummyItems(IForgeRegistry<Item> registry)
    {
        if(ModConfig.exposeDummyTestItems)
        {
            testItems = new TestItem[500];
            for (int i = 0; i < testItems.length; i++)
            {
                registry.register(testItems[i] = new TestItem("testitem"));

                NonNullList<ItemStack> items = NonNullList.create();
                testItems[i].getSubItems(EquivalentIntegrationsMod.creativeTab, items);

                for (int m = 0; m < items.size(); m++)
                {
                    ProjectEAPI.getEMCProxy().registerCustomEMC(items.get(m), (long) (i * items.size() + m + 1));
                }
            }
        }
    }
}
