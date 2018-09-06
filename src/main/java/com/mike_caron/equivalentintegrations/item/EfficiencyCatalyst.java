package com.mike_caron.equivalentintegrations.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class EfficiencyCatalyst extends ItemBase
{
    public static final String id = "efficiency_catalyst";

    public EfficiencyCatalyst()
    {
        setRegistryName(id);
        setTranslationKey(id);
        setMaxStackSize(4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        String tip;
        tip = I18n.format("item.efficiency_catalyst.desc1");
        if(!tip.isEmpty())
        {
            tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + tip);
        }
    }
}
