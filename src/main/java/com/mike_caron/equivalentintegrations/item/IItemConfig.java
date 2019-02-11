package com.mike_caron.equivalentintegrations.item;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IItemConfig
{
    void onConfig(@Nonnull ItemStack stack, int discriminator, int payload);
}
