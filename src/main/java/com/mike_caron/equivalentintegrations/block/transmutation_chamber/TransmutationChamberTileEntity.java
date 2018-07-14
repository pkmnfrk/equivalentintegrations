package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TransmutationChamberTileEntity extends TileEntity implements ITickable
{
    private UUID owner = null;

    private EMCItemHandler emcItemHandler;

    private int ticksSinceLastCacheUpdate = 0;

    private final TransmutationChamberItemStackHandler talismanInventory = new TransmutationChamberItemStackHandler()
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            if(world.isRemote)
                return;

            ItemStack stack = this.getStackInSlot(0);
            UUID owner = SoulboundTalisman.getOwnerFromStack(stack);

            TransmutationChamberTileEntity.this.setOwner(owner);
            TransmutationChamberTileEntity.this.markDirty();
        }
        /*
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty())
                return ItemStack.EMPTY;

            if (!SoulboundTalisman.isBound(stack))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
        */
    };

    public ItemStackHandler getTalismanInventory()
    {
        return talismanInventory;
    }

    public void setOwner(UUID newOwner)
    {
        if (newOwner != owner)
        {
            this.owner = newOwner;
            this.markDirty();
            if(world != null)
            {

                if(!world.isRemote)
                {
                    if (emcItemHandler != null)
                    {
                        destroyEmcItemHandler();
                    }

                    if (newOwner != null)
                    {
                        createEmcItemHandler(newOwner);
                    }
                }

                //world.markBlockRangeForRenderUpdate(getPos(), getPos());

                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(getPos(), state, state, 3);
            }
        }
    }

    private void createEmcItemHandler(UUID newOwner)
    {
        emcItemHandler = new EMCItemHandler(newOwner, world);
        MinecraftForge.EVENT_BUS.register(emcItemHandler);
    }

    private void destroyEmcItemHandler()
    {
        MinecraftForge.EVENT_BUS.unregister(emcItemHandler);
        emcItemHandler = null;
    }

    public boolean hasOwner()
    {
        return owner != null;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new SPacketUpdateTileEntity(getPos(), 1, nbt);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
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
            talismanInventory.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
    }

    @Override
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

        compound.setTag("items", talismanInventory.serializeNBT());

        //EquivalentIntegrationsMod.logger.info("Writing To NBT:");
        //EquivalentIntegrationsMod.logger.info(compound.toString());

        return compound;
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D; //8 blocks
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(talismanInventory);
            }
            //if the facing has an actual value, assume they mean the EMC inventory
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(emcItemHandler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update()
    {
        if(world.isRemote) return;

        if(emcItemHandler == null && owner != null)
        {
            //things can get weird right at world load, so this is a fallback just in case
            createEmcItemHandler(owner);
        }
        if(emcItemHandler != null)
        {
            ticksSinceLastCacheUpdate += 1;
            if (ticksSinceLastCacheUpdate >= 20)
            {
                emcItemHandler.refresh(false);
                ticksSinceLastCacheUpdate = 0;
            }
        }
    }
}
