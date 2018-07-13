package com.mike_caron.equivalentintegrations.block;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.OfflineEMCWorldData;
import com.mike_caron.equivalentintegrations.item.ModItems;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.asm.transformers.ItemStackTransformer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class TransmutationChamberTileEntity extends TileEntity implements IItemHandlerModifiable
{
    private UUID owner = null;
    private List<ItemStack> cachedKnowledge;
    private HashMap<ItemStack, Integer> cachedInventory;
    private double cachedEmc;

    private ItemStackHandler talismanInventory = new ItemStackHandler(1)
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

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (stack == ItemStack.EMPTY)
                return ItemStack.EMPTY;

            if (!SoulboundTalisman.isBound(stack))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
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
            refreshCachedKnowledge();
            world.markBlockRangeForRenderUpdate(getPos(), getPos());
            this.markDirty();
        }
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
            owner = UUID.fromString(compound.getString("owner"));
            refreshCachedKnowledge();
        }
        else
        {
            owner = null;
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
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        //throw new RuntimeException("Cannot set any stacks in this inventory.");

        //this method intentionally left blank
    }

    @Override
    public int getSlots()
    {
        if (owner == null)
            return 0;

        EquivalentIntegrationsMod.logger.info("Transmutation Chamber: Getting slot count");

        return cachedKnowledge.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);

        EquivalentIntegrationsMod.logger.info("Transmutation Chamber: Getting stack in slot " + slot);

        ItemStack stack = cachedKnowledge.get(slot);

        ItemStack ret = new ItemStack(stack.getItem(), cachedInventory.get(stack), stack.getMetadata(), stack.getTagCompound());

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

        if (world.isRemote)
        {
            throw new RuntimeException("What, this is running on the client??");
        }

        double emc = getRealEMC(owner);

        IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();

        //first off, what's in this stack?
        ItemStack desired = cachedKnowledge.get(slot);
        long emcCost = emcProxy.getValue(desired);

        int actualAmount = amount;

        //are we even capable of servicing this request?
        long desiredEMC = actualAmount * emcCost;

        if (desiredEMC > emc)
        {
            //hmm, that's unfortunate. How many _can_ we do?
            actualAmount = howManyCanWeMake(emc, emcCost);

            desiredEMC = actualAmount * emcCost;
        }

        //now we know that actualAmount is how many we can do.

        if (!simulate && desiredEMC > 0)
        {
            emc -= desiredEMC;
            setRealEMC(owner, emc);

            refreshCachedKnowledge();
        }

        return new ItemStack(desired.getItem(), actualAmount, desired.getMetadata(), desired.getTagCompound());
    }

    @Override
    public int getSlotLimit(int slot)
    {
        validateSlotIndex(slot);
        //IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        return Integer.MAX_VALUE;
    }

    @SubscribeEvent
    public void onPlayerKnowledgeChange(PlayerKnowledgeChangeEvent event)
    {
        if(event.getPlayerUUID() == owner)
        {
            EquivalentIntegrationsMod.logger.info("Refreshing cached knowledge due to knowledge change");
            refreshCachedKnowledge();
        }
    }

    @SubscribeEvent
    public void onEMCRemap(EMCRemapEvent event)
    {
        EquivalentIntegrationsMod.logger.info("Refreshing cached knowledge due to global remap");
        refreshCachedKnowledge();
    }

    protected void validateSlotIndex(int slot)
    {
        int size = 0;
        if (owner != null)
        {
            size = cachedInventory.size();
        }

        if (slot < 0 || slot >= size)
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + size + ")");
    }

    private double getRealEMC(UUID owner)
    {
        EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

        if (player != null)
        {
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
            return knowledge.getEmc();
        }
        else
        {
            return OfflineEMCWorldData.get(world).getCachedEMC(owner);
        }
    }

    private void setRealEMC(UUID owner, double emc)
    {
        EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

        if (player != null)
        {
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
            knowledge.setEmc(emc);
            knowledge.sync(player);
        }
        else
        {
            OfflineEMCWorldData.get(world).setCachedEMC(owner, emc);
        }
    }

    private int howManyCanWeMake(double emc, long cost)
    {
        long tmp = Math.floorDiv((long) emc, cost);
        if (tmp > Integer.MAX_VALUE)
        {
            // er, let's cap it at that, shall we?
            return Integer.MAX_VALUE;
        }
        else
        {
            return (int) tmp;
        }
    }

    private void refreshCachedKnowledge()
    {
        if(owner == null)
        {
            cachedKnowledge = null;
        }
        else
        {
            boolean updateInv = false;

            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);

            {
                List<ItemStack> tmp = knowledge.getKnowledge();
                if(cachedKnowledge == null || tmp.hashCode() != cachedKnowledge.hashCode())
                {
                    cachedKnowledge = tmp;
                    updateInv = true;
                }
            }
            {
                double tmp = getRealEMC(owner);
                if (cachedEmc != tmp)
                {
                    cachedEmc = tmp;
                    updateInv = true;
                }
            }

            if(updateInv)
            {
                IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();
                double emc = getRealEMC(owner);
                for (ItemStack is : cachedKnowledge)
                {
                    cachedInventory.put(is, howManyCanWeMake(emc, emcProxy.getValue(is)));
                }
            }
        }


    }
}
