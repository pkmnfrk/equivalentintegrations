package com.mike_caron.equivalentintegrations.storage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.OfflineEMCWorldData;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class EMCItemHandler implements IItemHandlerModifiable
{
    private final UUID owner;
    private final World world;

    private List<ItemStack> cachedKnowledge = null;
    private List<ItemStack> cachedInventory = null;
    private double cachedEmc = -1;

    private boolean needsFullRefresh = false;

    public EMCItemHandler(@Nonnull UUID owner,@Nonnull World world)
    {
        this.owner = owner;
        this.world = world;

        refresh(true);
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
        EquivalentIntegrationsMod.logger.trace("Transmutation Chamber: Getting slot count");

        if(!refresh(false)) {
            return 0;
        }

        EquivalentIntegrationsMod.logger.trace("Transmutation Chamber: Success");
        return cachedInventory.size() + 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if(!validateSlotIndex(slot, false))
            return ItemStack.EMPTY;

        //EquivalentIntegrationsMod.logger.info("Transmutation Chamber: Getting stack in slot " + slot);

        if(!refresh(false)) {
            return ItemStack.EMPTY;
        }

        if(slot == cachedInventory.size())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = cachedInventory.get(slot);

        return stack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if(!validateSlotIndex(slot, false))
            return stack;

        IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();

        if(emcProxy.hasValue(stack)){
            IKnowledgeProvider knowledge;

            knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);

            if(knowledge.hasKnowledge(stack))
            {
                double emc = getRealEMC(owner);

                long singleValue = emcProxy.getValue(stack);

                long emcValue = singleValue * stack.getCount();

                EquivalentIntegrationsMod.logger.debug("Burning a stack ({}) for {} EMC each, a total of {} (Simulation: {})", stack, singleValue, emcValue, simulate);

                if(!simulate)
                {
                    setRealEMC(owner, (double)(emc + emcValue));

                    refreshCachedKnowledge(false);
                }

                return ItemStack.EMPTY;

            }

        }

        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        validateSlotIndex(slot, true);

        if(slot == cachedInventory.size())
        {
            return ItemStack.EMPTY;
        }

        double emc = getRealEMC(owner);

        IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();

        //first off, what's in this stack?
        ItemStack desired = cachedInventory.get(slot);
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

        EquivalentIntegrationsMod.logger.debug("Materializing {} x ({}) for {} EMC each, a total of {} (Simulation: {})", actualAmount, desired, emcCost, desiredEMC, simulate);
        if(actualAmount < amount) {
            EquivalentIntegrationsMod.logger.debug("Sadly, this was less than the {} that was asked for", amount);
        }

        if (!simulate && desiredEMC > 0)
        {
            emc -= desiredEMC;
            setRealEMC(owner, emc);

            refreshCachedKnowledge(false);
        }

        return new ItemStack(desired.getItem(), actualAmount, desired.getMetadata(), desired.getTagCompound());
    }

    @Override
    public int getSlotLimit(int slot)
    {
        if(!validateSlotIndex(slot, false))
            return 0;
        //IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        return Integer.MAX_VALUE;
    }

    protected boolean validateSlotIndex(int slot, boolean fatal)
    {
        int size = cachedInventory.size() + 1;

        if (slot < 0 || slot >= size)
        {
            if(fatal)
            {
                throw new RuntimeException("Slot " + slot + " not in valid range - [0," + size + ")");
            }
            EquivalentIntegrationsMod.logger.warn("Slot " + slot + " not in valid range - [0," + size + ")");
            return false;
        }

        return true;
    }

    private double getRealEMC(UUID owner)
    {
        EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);

        double ret;

        if(player == null && OfflineEMCWorldData.get(world).hasCachedEMC(owner))
        {
            EquivalentIntegrationsMod.logger.debug("Retrieving cached EMC value for {}", owner);
            return OfflineEMCWorldData.get(world).getCachedEMC(owner);
        }

        EquivalentIntegrationsMod.logger.debug("Retrieving live EMC value for {}", owner);
        IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        return knowledge.getEmc();


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

    private static int howManyCanWeMake(double emc, long cost)
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

    public boolean refresh(boolean comprehensive)
    {
        if(owner == null) return true;

        boolean ret = true;

        if(cachedKnowledge == null)
        {
            try
            {
                ret = refreshCachedKnowledge(comprehensive);
            } catch(IllegalStateException ex)
            {
                EquivalentIntegrationsMod.logger.warn("Unable to refresh knowledge, due to something");
                ret = false;
            }
        }

        if(!ret && comprehensive) {
            needsFullRefresh = true;
        }
        return true;
    }

    private boolean refreshCachedKnowledge(boolean comprehensive)
            throws IllegalStateException
    {
        if(world == null) return false;
        if(world.isRemote) return true;

        if(needsFullRefresh)
        {
            comprehensive = true;
        }

        if(owner == null)
        {
            cachedKnowledge = null;
            cachedEmc = -1;
            cachedInventory = null;
        }
        else
        {
            boolean updateInv = false;

            if(comprehensive){
                IKnowledgeProvider knowledge;
                knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);

                List<ItemStack> tmp = knowledge.getKnowledge();

                cachedKnowledge = tmp;
                updateInv = true;

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
                cachedInventory = new ArrayList<>();

                for(int i = 0; i < cachedKnowledge.size(); i++)
                {
                    ItemStack is = cachedKnowledge.get(i);
                    int num = howManyCanWeMake(emc, emcProxy.getValue(is));

                    if(num > 0) {
                        cachedInventory.add(new ItemStack(is.getItem(), num, is.getMetadata(), is.getTagCompound()));
                    }
                }
            }
        }

        needsFullRefresh = false;
        //EquivalentIntegrationsMod.logger.info("Successfully refreshed cache");

        return true;

    }

    @SubscribeEvent
    public void onEMCRemap(EMCRemapEvent event)
    {
        EquivalentIntegrationsMod.logger.info("Refreshing cached knowledge due to global remap");
        refresh(true);
    }

    @SubscribeEvent
    public void onPlayerKnowledgeChange(PlayerKnowledgeChangeEvent event)
    {
        if(event.getPlayerUUID().equals(owner))
        {
            EquivalentIntegrationsMod.logger.info("Refreshing cached knowledge due to knowledge change");
            refreshCachedKnowledge(true);
        }
    }

}
