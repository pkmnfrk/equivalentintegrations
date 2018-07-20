package com.mike_caron.equivalentintegrations.storage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.api.capabilities.IEMCManager;
import com.mike_caron.equivalentintegrations.api.events.EMCChangedEvent;
import com.mike_caron.equivalentintegrations.impl.EMCManagerProvider;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.EMCRemapEvent;
//import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.NBTWhitelist;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.asm.transformers.ItemStackTransformer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.Sys;

import javax.annotation.Nonnull;
import java.util.*;

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
    private boolean needsRefresh = false;

    private boolean needsFullRefresh = false;

    private IEMCProxy emcProxy;
    private IEMCManager emcManager;

    private long timeInGetCount, numInGetCount;
    private long timeInGetSlot, numInGetSlot;
    private long timeInExtract, numInExtract;
    private long timeInInsert, numInInsert;
    private long timeInEmcProxy, numInEmcProxy;
    private long timeInRefreshKnowledge, numInRefreshKnowledge;

    private long profilingStartTime;

    private final Map<Item, ItemStack> cachedStacks = new HashMap<>();

    public EMCItemHandler(@Nonnull UUID owner,@Nonnull World world)
    {
        this.owner = owner;
        this.world = world;

        this.emcProxy = ProjectEAPI.getEMCProxy();
        this.emcManager = world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

        profilingStartTime = System.nanoTime();

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
        long startTime = System.nanoTime();
        numInGetCount ++;
        //EquivalentIntegrationsMod.logger.trace("Transmutation Chamber: Getting slot count");

        if(cachedKnowledge == null)
        {
            if (!refresh(false))
            {
                timeInGetCount += System.nanoTime() - startTime;
                return 0;
            }
        }

        //EquivalentIntegrationsMod.logger.trace("Transmutation Chamber: Success");
        int ret = cachedInventory.size() + 64;

        timeInGetCount += System.nanoTime() - startTime;

        return ret;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
{
    long startTime = System.nanoTime();
    numInGetSlot ++;

    if(!validateSlotIndex(slot, false))
    {
        //timeInGetSlot += System.nanoTime() - startTime;
        return ItemStack.EMPTY;
    }

    if(cachedInventory == null)
    {
        if (!refresh(false))
        {
            timeInGetSlot += System.nanoTime() - startTime;
            return ItemStack.EMPTY;
        }
    }

    if(slot >= cachedInventory.size())
    {
        //timeInGetSlot += System.nanoTime() - startTime;
        return ItemStack.EMPTY;
    }

    timeInGetSlot += System.nanoTime() - startTime;
    return cachedInventory.get(slot);
}

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        long startTime = System.nanoTime();
        numInInsert ++;
        if(!validateSlotIndex(slot, false))
        {
            timeInInsert += System.nanoTime() - startTime;
            return stack;
        }

        if(emcProxy.hasValue(stack)){
            IKnowledgeProvider knowledge;

            knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);

            if(canLearn || knowledge.hasKnowledge(stack))
            {


                double emc = emcManager.getEMC(owner);

                long singleValue = emcManager.getEmcSellValue(stack);

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

                        //TODO: When 1.3.1 comes out, put this back in
                        //EntityPlayer player = world.getPlayerEntityByUUID(owner);
                        //if (!MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, stack))) //Only show the "learned" text if the knowledge was added
                        //{
                            //note: this will not work if the user is offline. In this case, the later
                            //knowledge check will return false, thus rejecting the item
                            knowledge.addKnowledge(stack);
                        //}

                    }

                    needsRefresh = true;
                }

                timeInInsert += System.nanoTime() - startTime;
                return ItemStack.EMPTY;
            }
        }

        timeInInsert += System.nanoTime() - startTime;
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        long startTime = System.nanoTime();
        numInExtract ++;

        validateSlotIndex(slot, true);

        if(slot >= cachedInventory.size())
        {
            timeInExtract += System.nanoTime() - startTime;
            return ItemStack.EMPTY;
        }

        //IEMCManager emcManager = world.getCapability(EMCManagerProvider.EMC_MANAGER_CAPABILITY, null);

        double emc = emcManager.getEMC(owner);

        //IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();

        //first off, what's in this stack?
        ItemStack desired = cachedInventory.get(slot);
        long emcCost = emcManager.getEmcValue(desired);

        if(emcCost == 0)
        {
            timeInExtract += System.nanoTime() - startTime;
            return ItemStack.EMPTY;
        }

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

            //refreshCachedKnowledge(false);
        }



        //EquivalentIntegrationsMod.logger.info("Returning stack with id {}", System.identityHashCode(ret));

        timeInExtract += System.nanoTime() - startTime;
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
        if(cost == 0) return 0;
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

        needsRefresh = !ret;
        return ret;
    }

    private void refreshCachedKnowledge(boolean comprehensive)
            throws IllegalStateException
    {
        if(world.isRemote) return;

        long startTime = System.nanoTime();
        numInRefreshKnowledge ++;

        if(needsFullRefresh)
        {
            comprehensive = true;
        }

        boolean updateInv = false;

        refresh_part1(comprehensive);
        refresh_part2();


        needsFullRefresh = false;
        //EquivalentIntegrationsMod.logger.info("Successfully refreshed cache");

        timeInRefreshKnowledge = System.nanoTime() - startTime;
    }

    private void refresh_part1(boolean comprehensive)
    {
        boolean updateInv;
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
    }

    private void refresh_part2()
    {
        if(cachedInventory != null)
        {
            //EquivalentIntegrationsMod.logger.info("Clearing array of size {}", cachedInventory.size());
            //cachedInventory.clear();
        }
        else
        {
            cachedInventory = new ArrayList<>();
        }

        //cachedInventory = new ArrayList<>();


        int ix = 0;
        int addedNew = 0;
        int overwrote = 0;
        int updated = 0;

        for(int jx = 0; jx < cachedKnowledge.size(); jx++)
        {
            ItemStack is = cachedKnowledge.get(jx);

            long value = emcManager.getEmcValue(is);
            if(value == 0)
            {
                continue;
            }

            int num = howManyCanWeMake(cachedEmc, value);

            if(num > 0)
            {
                if (ix < cachedInventory.size())
                {
                    ItemStack cached = cachedInventory.get(ix);

                    if (cached.getItem() == is.getItem())
                    {
                        cached.setCount(num);
                        updated ++;
                    }
                    else
                    {
                        cachedInventory.set(ix, new ItemStack(is.getItem(), num, is.getMetadata(), is.getTagCompound()));
                        overwrote ++;
                    }
                }
                else
                {
                    cachedInventory.add(new ItemStack(is.getItem(), num, is.getMetadata(), is.getTagCompound()));
                    addedNew ++;
                }

                ix += 1;
            }
        }

        //EquivalentIntegrationsMod.logger.info("Refreshed knowledge. Updated {}, Overwrote {}, Added {}, Trimmed {}", updated, overwrote, addedNew, cachedInventory.size() - ix);

        while(cachedInventory.size() > ix)
        {
            cachedInventory.remove(ix);
        }
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
            refresh(true);
        }
    }

    @SubscribeEvent
    public void onEmcChanged(EMCChangedEvent event)
    {
        //refresh(true);
        if(event.player.equals(owner))
        {
            cachedEmc = event.newEmc;
            needsRefresh = true;
        }

        outputProfilingData();
    }

    public static void cleanupKnowledge(EntityPlayer player)
    {
        IKnowledgeProvider knowledge;

        knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(player.getUniqueID());

        List<ItemStack> oldKnowledge = new ArrayList<>(knowledge.getKnowledge());

        knowledge.clearKnowledge();

        for(ItemStack stack : oldKnowledge)
        {
            if(stack.isItemStackDamageable() && stack.isItemDamaged())
            {
                stack.setItemDamage(0);
            }

            if(stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack))
            {
                stack.setTagCompound(null);
            }

            if(!knowledge.hasKnowledge(stack))
            {
                knowledge.addKnowledge(stack);
            }
        }

        knowledge.sync((EntityPlayerMP)player);

    }

    private void outputProfilingData()
    {

        long deltaTime = System.nanoTime() - profilingStartTime;
        if (deltaTime > 1000000000)
        {
            if (false)
            {
                EquivalentIntegrationsMod.logger.warn("In the last {} us, we spent:", deltaTime);
                EquivalentIntegrationsMod.logger.warn("Time in EMC Proxy: {} over {} calls", timeInEmcProxy / 1000000f, numInEmcProxy);
                EquivalentIntegrationsMod.logger.warn("Time in getSlots(): {} over {} calls", timeInGetCount / 1000000f, numInGetCount);
                EquivalentIntegrationsMod.logger.warn("Time in getStackInSlots(): {} over {} calls", timeInGetSlot / 1000000f, numInGetSlot);
                EquivalentIntegrationsMod.logger.warn("Time in extractItem(): {} over {} calls", timeInInsert / 1000000f, numInInsert);
                EquivalentIntegrationsMod.logger.warn("Time in insertItem(): {} over {} calls", timeInExtract / 1000000f, numInExtract);
                EquivalentIntegrationsMod.logger.warn("Time in refreshKnowledge(): {} over {} calls", timeInRefreshKnowledge / 1000000f, numInRefreshKnowledge);
            }
            timeInEmcProxy = 0;
            numInEmcProxy = 0;
            timeInGetCount = 0;
            numInGetCount = 0;
            timeInGetSlot = 0;
            numInGetSlot = 0;
            timeInInsert = 0;
            numInInsert = 0;
            timeInExtract = 0;
            numInExtract = 0;
            timeInRefreshKnowledge = 0;
            numInRefreshKnowledge = 0;

            profilingStartTime = System.nanoTime();

        }
    }

    public void tick()
    {
        if(needsRefresh)
        {
            refresh(true);
        }

        outputProfilingData();
    }
}
