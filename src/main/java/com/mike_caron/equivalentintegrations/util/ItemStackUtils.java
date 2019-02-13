package com.mike_caron.equivalentintegrations.util;

import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class ItemStackUtils
{
    public ItemStackUtils() {}

    @Nullable
    public static UUID getPlayerUUID(@Nonnull ItemStack stack)
    {
        NBTTagCompound nbt = ItemUtils.getItemTag(stack);
        if(!nbt.hasKey("player"))
            return null;

        return UUID.fromString(nbt.getString("player"));
    }
}
