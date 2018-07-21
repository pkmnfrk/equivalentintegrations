package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.integrations.ITOPInfoProvider;
import com.mike_caron.equivalentintegrations.inventory.SoulboundTalismanSlot;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class TransmutationChamberContainer
        extends Container
{
    private final TransmutationChamberTileEntity te;
    private Slot talismanSlot;
    private Slot algorithmsSlot;
    private Slot efficiencySlot;

    public TransmutationChamberContainer(IInventory playerInventory, TransmutationChamberTileEntity te)
    {
        this.te = te;

        addOwnSlots();
        addPlayerSlots(playerInventory);
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

        talismanSlot = addSlotToContainer(new SoulboundTalismanSlot(itemHandler, 0, 47, 27));
        algorithmsSlot = addSlotToContainer(new SlotItemHandler(itemHandler, 1, 101, 27));
        efficiencySlot = addSlotToContainer(new SlotItemHandler(itemHandler, 2, 119, 27));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < TransmutationChamberItemStackHandler.NUM_SLOTS)
            { //transferring from block -> player
                if (!this.mergeItemStack(itemstack1, TransmutationChamberItemStackHandler.NUM_SLOTS, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                if (!this.mergeItemStack(itemstack1, 0, TransmutationChamberItemStackHandler.NUM_SLOTS, false))
                { //transferring from player -> block
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {

                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return te.canInteractWith(playerIn);
    }
}
