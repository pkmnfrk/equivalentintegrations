package com.mike_caron.equivalentintegrations.inventory;

import com.mike_caron.equivalentintegrations.util.OptionalInt;
import net.minecraft.inventory.IInventory;

public interface IInventoryCallback {
    public void onInventoryChanged(IInventory inventory, OptionalInt slotNumber);
}
