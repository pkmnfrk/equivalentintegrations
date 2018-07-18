package com.mike_caron.equivalentintegrations.storage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.api.capabilities.IEMCManager;
import com.mike_caron.equivalentintegrations.api.events.EMCChangedEvent;
import com.mike_caron.equivalentintegrations.impl.EMCManagerProvider;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.NBTWhitelist;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EMCItemHandler implements IItemHandlerModifiable
{
    @Nonnull
    private final UUID owner;
    @Nonnull
    private final World world;

    private List<ItemStack> cachedKnowledge = null;
    private List<ItemStack> cachedInventory = null;
    private double cachedEmc = -1;

    private int efficiencyThreshold = 10;
    private boolean canLearn = false;

    private boolean needsFullRefresh = false;

    public EMCItemHandler(@Nonnull UUID owner,@Nonnull World world)
    {
        this.owner = owner;
        this.world = world;

        refresh(true);
    }

    @Nonnull
    public UUID getOwner()
    {
        return owner;
    }

    public void setEfficiencyThreshold(int efficiencyThreshold)
    {
        this.efficiencyThreshold = efficiencyThreshold;
    }

    public int getEfficiencyThreshold()
    {
        return this.efficiencyThreshold;
    }

    public void setCanLearn(boolean canLearn)
    {
        this.canLearn = canLearn;
    }

    public boolean getCanLearn()
    {
        return this.canLearn;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        //throw new RuntimeException("Cannot set any stacks in this inventory.");

        //this method intentionally left blank
    }

    @Override
    public int getSlots()
    {
        //EquivalentIntegrationsMod.logger.trace("Transmutation Chamber: Getting slot count");

        if(!refresh(false)) {
            return 0;
        }

        //EquivalentIntegrationsMod.logger.trace("Transmutation Chamber: Success");
        return cachedInventory.size() + 64;
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

        if(slot >= cachedInventory.size())
        {
            return ItemStack.EMPTY;
        }

        return cachedInventory.get(slot);
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

            if(canLearn || knowledge.hasKnowledge(stack))
            {
                IEMCManager emcManager = world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

                double emc = emcManager.getEMC(owner);

                long singleValue = EMCHelper.getEmcSellValue(stack);

                long emcValue = singleValue * stack.getCount();

                emcValue -= getEfficiencyCost(stack, emcValue);

                //EquivalentIntegrationsMod.logger.info("Burning a stack ({}, {}) for {} EMC each, a total of {} (Simulation: {})", stack, System.identityHashCode(stack) , singleValue, emcValue, simulate);

                if(!simulate)
                {
                    emcManager.setEMC(owner, emc + emcValue);

                    //then, clean up the stack a bit
                    if(ItemHelper.isDamageable(stack))
                    {
                        stack.setItemDamage(0);
                    }

                    stack.setCount(1);

                    if(canLearn && !knowledge.hasKnowledge(stack))
                    {

                        if (stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack))
                        {
                            stack.setTagCompound(null);
                        }

                        EntityPlayer player = world.getPlayerEntityByUUID(owner);
                        if (!MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, stack))) //Only show the "learned" text if the knowledge was added
                        {
                            //note: this will not work if the user is offline. In this case, the later
                            //knowledge check will return false, thus rejecting the item
                            knowledge.addKnowledge(stack);
                        }

                    }

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

        if(slot >= cachedInventory.size())
        {
            return ItemStack.EMPTY;
        }

        IEMCManager emcManager = world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

        double emc = emcManager.getEMC(owner);

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

        ItemStack ret = new ItemStack(desired.getItem(), actualAmount, desired.getMetadata(), desired.getTagCompound());

        //before this point, desiredEMC is guaranteed to be < emc
        //but, the efficiency cost may put it over
        desiredEMC += getEfficiencyCost(ret, desiredEMC);

        //EquivalentIntegrationsMod.logger.debug("Materializing {} x ({}) for {} EMC each, a total of {} (Simulation: {})", actualAmount, desired, emcCost, desiredEMC, simulate);
        //if(actualAmount < amount) {
        //    EquivalentIntegrationsMod.logger.debug("Sadly, this was less than the {} that was asked for", amount);
        //}

        //if that happens, rather than trying to solve this calculus, just round it off
        if(desiredEMC > emc)
        {
            desiredEMC = (long)emc;
        }

        if (!simulate && desiredEMC > 0)
        {
            emc -= desiredEMC;
            emcManager.setEMC(owner, emc);

            refreshCachedKnowledge(false);
        }



        //EquivalentIntegrationsMod.logger.info("Returning stack with id {}", System.identityHashCode(ret));

        return ret;
    }

    private int getEfficiencyCost(ItemStack stack, long emcCost)
    {
        if(emcCost < efficiencyThreshold) return 0;

        return stack.getCount();
    }

    @Override
    public int getSlotLimit(int slot)
    {
        if(!validateSlotIndex(slot, false))
            return 0;
        //IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);
        return Integer.MAX_VALUE;
    }

    private boolean validateSlotIndex(int slot, boolean fatal)
    {
        int size = cachedInventory.size() + 64;

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
        boolean ret = true;

        //if(cachedKnowledge == null)
       // {
            try
            {
                refreshCachedKnowledge(comprehensive);
                ret = true;
            } catch(IllegalStateException ex)
            {
                EquivalentIntegrationsMod.logger.warn("Unable to refresh knowledge, due to something");
                ret = false;
            }
       //}

        if(!ret && comprehensive) {
            needsFullRefresh = true;
        }
        return ret;
    }

    private void refreshCachedKnowledge(boolean comprehensive)
            throws IllegalStateException
    {
        if(world.isRemote) return;

        IEMCManager emcManager = world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

        if(needsFullRefresh)
        {
            comprehensive = true;
        }

        boolean updateInv = false;

        if(comprehensive){
            IKnowledgeProvider knowledge;
            knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);

            cachedKnowledge = knowledge.getKnowledge();

            updateInv = true;

        }
        {
            double tmp = emcManager.getEMC(owner);
            if (cachedEmc != tmp)
            {
                cachedEmc = tmp;
                updateInv = true;
            }
        }

        if(updateInv)
        {
            IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();
            double emc = emcManager.getEMC(owner);
            cachedInventory = new ArrayList<>();

            for(ItemStack is : cachedKnowledge)
            {
                int num = howManyCanWeMake(emc, emcProxy.getValue(is));

                if(num > 0) {
                    cachedInventory.add(new ItemStack(is.getItem(), num, is.getMetadata(), is.getTagCompound()));
                }
            }
        }

        needsFullRefresh = false;
        //EquivalentIntegrationsMod.logger.info("Successfully refreshed cache");

    }

    @SuppressWarnings("unused")
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

    public void onEmcChanged(EMCChangedEvent event)
    {
        refresh(true);
    }

}
