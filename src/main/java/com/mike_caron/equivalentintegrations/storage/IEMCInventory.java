package com.mike_caron.equivalentintegrations.storage;

import net.minecraft.item.ItemStack;

import java.util.Collection;

public interface IEMCInventory
{
    Collection<ItemStack> getCachedInventory();
}
