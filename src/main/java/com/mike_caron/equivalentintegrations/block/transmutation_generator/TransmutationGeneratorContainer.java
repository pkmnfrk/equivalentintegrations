package com.mike_caron.equivalentintegrations.block.transmutation_generator;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class TransmutationGeneratorContainer extends Container
{
    private final TransmutationGeneratorTileEntity te;
    private Slot talismanSlot;
    private Slot efficiencySlot;

    public TransmutationGeneratorContainer(IInventory playerInventory, TransmutationGeneratorTileEntity te)
    {
        this.te = te;

        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
    }

    private void addPlayerSlots(IInventory playerInventory)
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
            this.addSlotToContainer(new Slot(playerInventory, col, x, y));
        }
    }

    private void addOwnSlots()
    {
        IItemHandler itemHandler = this.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        talismanSlot = addSlotToContainer(new SlotItemHandler(itemHandler, 0, 29, 27));
        efficiencySlot = addSlotToContainer(new SlotItemHandler(itemHandler, 1, 61, 27));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < TransmutationGeneratorStackHandler.NUM_SLOTS)
            { //transferring from block -> player
                EquivalentIntegrationsMod.logger.info("Transferring {} into the player", itemstack1);
                if (!this.mergeItemStack(itemstack1, TransmutationGeneratorStackHandler.NUM_SLOTS, this.inventorySlots.size(), true)) {
                    EquivalentIntegrationsMod.logger.info("Transferred {} into the player", itemstack1);
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                EquivalentIntegrationsMod.logger.info("Transferring {} into the block", itemstack1);
                if (!this.mergeItemStack(itemstack1, 0, TransmutationGeneratorStackHandler.NUM_SLOTS, false))
                { //transferring from player -> block
                    EquivalentIntegrationsMod.logger.info("Transferred {} into the block", itemstack1);
                    return ItemStack.EMPTY;
                }
            }

            EquivalentIntegrationsMod.logger.info("Itemstack1: {}", itemstack1);
            if (itemstack1.isEmpty()) {

                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        else
        {
            EquivalentIntegrationsMod.logger.info("Did nothing");
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return te.canInteractWith(playerIn);
    }
}
