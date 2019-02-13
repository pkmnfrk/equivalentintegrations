package com.mike_caron.equivalentintegrations.util;

import javax.annotation.Nullable;
import java.util.UUID;
public final class ItemUtils
{
    public ItemUtils() {}

    }

    @Nullable
    public static UUID getPlayerUUID(@Nonnull ItemStack stack)
    {
        NBTTagCompound nbt = getItemTag(stack);
        if(!nbt.hasKey("player"))
            return null;

        return UUID.fromString(nbt.getString("player"));
    }
}
