package com.mike_caron.equivalentintegrations.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public final class ItemUtils
{
    public ItemUtils() {}

    @Nonnull
    public static NBTTagCompound getItemTag(@Nonnull ItemStack stack)
    {
        NBTTagCompound ret = stack.getTagCompound();
        if(ret == null)
        {
            ret = new NBTTagCompound();
            stack.setTagCompound(ret);
        }
        return ret;
    }
}
