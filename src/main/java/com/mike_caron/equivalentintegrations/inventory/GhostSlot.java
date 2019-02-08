package com.mike_caron.equivalentintegrations.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class GhostSlot
    extends Slot
{
    public GhostSlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        super.decrStackSize(amount);
        return ItemStack.EMPTY;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return 1;
    }

    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
    {
        super.onTake(thePlayer, stack);
        return ItemStack.EMPTY;
    }

    @Override
    public void putStack(ItemStack stack)
    {
        super.putStack(stack);
    }
}
