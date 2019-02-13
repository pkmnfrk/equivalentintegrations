package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.network.ItemConfigMessage;
import com.mike_caron.mikesmodslib.block.ContainerBase;
import com.mike_caron.mikesmodslib.inventory.GhostSlot;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class ConjurationAssemblerContainer
    extends ContainerBase
{
    private final InventoryPlayer playerInventory;
    private final ConjurationAssembler.Inventory containerInventory;
    private Slot filterSlot;

    private final int protectedSlot;
    private final int protectedIndex;

    public int currentColor;

    public static final int GUI_ID = 3;

    public ConjurationAssemblerContainer(IInventory playerInventory, ConjurationAssembler.Inventory containerInventory, int protectedIndex)
    {
        super(playerInventory);

        this.playerInventory = (InventoryPlayer)playerInventory;
        this.containerInventory = containerInventory;

        addOwnSlots();
        addPlayerSlots(playerInventory);

        this.protectedIndex = protectedIndex;
        this.protectedSlot = findSlotForIndex(protectedIndex);

        this.currentColor = containerInventory.getCurrentColor();
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if(currentColor != containerInventory.getCurrentColor())
        {
            currentColor = containerInventory.getCurrentColor();
            changed = true;
        }

        if(changed)
        {
            triggerUpdate();
        }
    }

    @Override
    protected void onReadNBT(NBTTagCompound tag)
    {
        super.onReadNBT(tag);

        changed = false;
        if(tag.getInteger("color") != currentColor)
        {
            currentColor = tag.getInteger("color");
            changed = true;
        }
    }

    @Override
    protected void onWriteNBT(NBTTagCompound tag)
    {
        super.onWriteNBT(tag);

        tag.setInteger("color", currentColor);
    }

    @Override
    public int getId()
    {
        return GUI_ID;
    }

    private int findSlotForIndex(int index)
    {
        for(Slot slot : inventorySlots)
        {
            if(slot.getSlotIndex() == protectedIndex && slot.inventory == playerInventory)
                return slot.slotNumber;
        }

        return -1;
    }

    @Override
    protected void addPlayerSlots(IInventory playerInventory)
    {
        // Slots for the main inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 11 + col * 18;
                int y = row * 18 + 71;
                this.addSlotToContainer(new Slot(playerInventory, (row + 1) * 9 + col, x, y));
            }
        }

        // Slots for the hotbar
        for (int col = 0; col < 9; ++col) {
            int x = 11 + col * 18;
            int y = 59 + 70;
            this.addSlotToContainer(new Slot(playerInventory, col, x, y) {
                @Override
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    return slotNumber != protectedSlot && super.canTakeStack(playerIn);
                }
            });
        }
    }

    @Override
    protected void addOwnSlots()
    {
        filterSlot = addSlotToContainer(new GhostSlot(this.containerInventory, 0, 83, 32)
        {
            @Override
            public boolean isItemValid(ItemStack stack)
            {
                if(!super.isItemValid(stack)) return false;

                if(!ProjectEAPI.getEMCProxy().hasValue(stack)) return false;

                return true;
            }
        });

    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(index == protectedSlot)
            return slot.getStack();

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 1)
            { //transferring from block -> player
                slot.putStack(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }
            else
            {
                /*
                //transferring from player -> block
                if(filterSlot.getStack().isEmpty() && filterSlot.isItemValid(itemstack))
                {
                    this.filterSlot.putStack(itemstack);
                }
                */

                if(index <= 27)
                {
                    if (!this.mergeItemStack(itemstack1, 28, 36, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else
                {
                    if (!this.mergeItemStack(itemstack1, 1, 27, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }



            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player)
    {
        if(slotId == protectedSlot)
            return ItemStack.EMPTY;
        if(clickType == ClickType.SWAP && dragType == protectedSlot)
            return ItemStack.EMPTY;
        if(clickType == ClickType.PICKUP && slotId == 0)
        {
            ItemStack held = playerInventory.getItemStack().copy();
            super.slotClick(slotId, dragType, clickType, player);
            playerInventory.setItemStack(held);
            return held;
        }
        if(clickType == ClickType.QUICK_MOVE)
        {
           if(slotId == 0)
           {
               filterSlot.putStack(ItemStack.EMPTY);
               return ItemStack.EMPTY;
           }
           else
           {
               ItemStack held = inventorySlots.get(slotId).getStack();
               if(filterSlot.getStack().isEmpty() && filterSlot.isItemValid(held))
               {
                   filterSlot.putStack(held);
                   return held;
               }
           }
        }

        return super.slotClick(slotId, dragType, clickType, player);
    }


    public void setCurrentColor(int color)
    {
        ItemConfigMessage msg = new ItemConfigMessage(1, color);
        EquivalentIntegrationsMod.networkWrapper.sendToServer(msg);

        currentColor = color;
        notifyGuiUpdate();
    }
}
