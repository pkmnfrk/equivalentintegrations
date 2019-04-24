package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.mikesmodslib.item.DebugItemBase;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;

public class TestItem
    extends DebugItemBase
{
    private int num;
    private static int numCounter = 1;
    private static IModel itemModel = null;

    public TestItem(String id)
    {
        super(id + "" + (numCounter ++));

        num = numCounter;

        setTranslationKey(id);
        setCreativeTab(EquivalentIntegrationsMod.creativeTab);
    }

    @Override
    public boolean getHasSubtypes()
    {
        return true;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if(this.isInCreativeTab(tab))
        {
            for(int i = 0; i < 100; i++)
            {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return super.getItemStackDisplayName(stack) + num + "-" + stack.getMetadata();
    }

    @Override
    public void initModel()
    {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation("item/alchemical_algorithms"));
    }
}
