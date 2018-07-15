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

public class AlchemicalAlgorithms extends ItemBase
{
    public static final String id = "alchemical_algorithms";

    public AlchemicalAlgorithms()
    {
        setRegistryName(id);
        setUnlocalizedName(id);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        String tip;

        tip = I18n.format("item.alchemical_algorithms.desc1");
        if(!tip.isEmpty())
        {
            tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + tip);
        }

        tip = I18n.format("item.alchemical_algorithms.desc2");
        if(!tip.isEmpty())
        {
            tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + tip);
        }
    }
}
