package com.mike_caron.equivalentintegrations.inventory;

import com.mike_caron.equivalentintegrations.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SoulboundTalismanSlot extends SlotItemHandler
{
    public SoulboundTalismanSlot(IItemHandler inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack.getItem() == ModItems.soulboundTalisman || stack.isEmpty();
    }
}
