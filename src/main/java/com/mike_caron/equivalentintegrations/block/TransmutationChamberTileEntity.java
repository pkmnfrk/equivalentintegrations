package com.mike_caron.equivalentintegrations.block;

import com.mike_caron.equivalentintegrations.item.ModItems;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TransmutationChamberTileEntity extends TileEntity implements IItemHandlerModifiable
{
    private UUID owner = null;

    private ItemStackHandler talismanInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot)
        {
            ItemStack stack = this.getStackInSlot(0);
            UUID owner = SoulboundTalisman.getOwnerFromStack(stack);

            TransmutationChamberTileEntity.this.setOwner(owner);
            TransmutationChamberTileEntity.this.markDirty();
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if(stack == ItemStack.EMPTY)
                return ItemStack.EMPTY;

            if(!SoulboundTalisman.isBound(stack))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    };

    public ItemStackHandler getTalismanInventory() {
        return talismanInventory;
    }

    public void setOwner(UUID newOwner)
    {
        if(newOwner != owner)
        {
            this.owner = newOwner;
            world.markBlockRangeForRenderUpdate(getPos(), getPos());
            this.markDirty();
        }
    }

    public boolean hasOwner()
    {
        return owner != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey("owner"))
        {
            owner = compound.getUniqueId("owner");
        }
        else
        {
            owner = null;
        }

        if(compound.hasKey("items")) {
            talismanInventory.deserializeNBT((NBTTagCompound)compound.getTag("items"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if(owner != null)
        {
            compound.setUniqueId("owner", owner);
        }
        else
        {
            if(compound.hasKey("owner")) {
                compound.removeTag("owner");
            }
        }

        compound.setTag("items", talismanInventory.serializeNBT());

        return compound;
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D; //8 blocks
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //only return the talismanInventory if the call is coming from inside the house
            if(facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(talismanInventory);
            }
            //if the facing has an actual value, assume they mean the EMC inventory
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        throw new RuntimeException("Cannot set any stacks in this inventory.");
    }

    @Override
    public int getSlots()
    {
        if(owner == null) return 0;

        IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        return knowledge.getKnowledge().size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);

        IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();

        ItemStack stack = knowledge.getKnowledge().get(slot);

        double emc = knowledge.getEmc();

        long numItems = Math.floorDiv((long)emc, emcProxy.getValue(stack));
        if(numItems > Integer.MAX_VALUE)
        {
            numItems = Integer.MAX_VALUE;
        }

        ItemStack ret = new ItemStack(stack.getItem(), (int)numItems, stack.getMetadata(), stack.getTagCompound());

        return ret;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        validateSlotIndex(slot);
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        validateSlotIndex(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        validateSlotIndex(slot);
        IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        return 0;
    }

    protected void validateSlotIndex(int slot)
    {
        int size = 0;
        if(owner != null) {
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
            size = knowledge.getKnowledge().size();
        }

        if (slot < 0 || slot >= size)
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + size + ")");
    }
}
