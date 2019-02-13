package com.mike_caron.equivalentintegrations;

import com.mike_caron.equivalentintegrations.item.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTab extends CreativeTabs
{
    public CreativeTab()
    {
        super(EquivalentIntegrationsMod.modId);
    }

    @Override
    public ItemStack createIcon()
    {
        return new ItemStack(ModItems.efficiencyCatalyst, 1);
    }


}
