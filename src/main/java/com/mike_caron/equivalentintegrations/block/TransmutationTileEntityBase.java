package com.mike_caron.equivalentintegrations.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class TransmutationTileEntityBase extends TileEntity
{
    @Nullable
    protected UUID owner;

    @Nonnull
    protected final ItemStackHandler inventory;

    public TransmutationTileEntityBase()
    {
        inventory = createInventory();
    }

    @Nonnull
    public ItemStackHandler getInventory()
    {
        return inventory;
    }

    @Nonnull
    protected abstract ItemStackHandler createInventory();

    public boolean hasOwner()
    {
        return owner != null;
    }

    @Nonnull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new SPacketUpdateTileEntity(getPos(), 1, nbt);
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    public void setOwner(UUID newOwner)
    {
        if (newOwner != owner)
        {
            UUID oldOwner = owner;

            this.owner = newOwner;
            this.markDirty();
            if(world != null)
            {

                onNewOwner(oldOwner);

                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(getPos(), state, state, 3);
            }
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("owner"))
        {
            setOwner(UUID.fromString(compound.getString("owner")));
        }
        else
        {
            setOwner(null);
        }

        if (compound.hasKey("items"))
        {
            inventory.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (owner != null)
        {
            compound.setString("owner", owner.toString());
        }
        else
        {
            if (compound.hasKey("owner"))
            {
                compound.removeTag("owner");
            }
        }

        compound.setTag("items", inventory.serializeNBT());

        return compound;
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D; //8 blocks
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            //only return the talismanInventory if the call is coming from inside the house
            if (facing == null)
            {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
            }
        }
        return super.getCapability(capability, facing);
    }

    protected void onNewOwner(UUID oldOwner) {}

    protected void notifyUpdate()
    {
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }
}
