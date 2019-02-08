package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.util.MappedModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemBase extends Item
{
    public ItemBase()
    {
        setCreativeTab(EquivalentIntegrationsMod.creativeTab);
    }
    @SuppressWarnings("ConstantConditions")
    @SideOnly(Side.CLIENT)
    public void initModel(MappedModelLoader.Builder modelLoader) {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
