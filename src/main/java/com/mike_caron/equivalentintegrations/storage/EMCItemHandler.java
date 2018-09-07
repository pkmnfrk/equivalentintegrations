package com.mike_caron.equivalentintegrations.storage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.api.events.EMCChangedEvent;
import com.mike_caron.equivalentintegrations.impl.ManagedEMCManager;
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
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.Sys;

import javax.annotation.Nonnull;
import java.util.*;

public final class EMCItemHandler
    implements IItemHandlerModifiable, IEMCInventory
{
    @Nonnull
    private final UUID owner;
    @Nonnull
    private final World world;

    private int efficiencyThreshold = 10;
    private boolean canLearn = false;

    private boolean forbidNbt = false;
    private boolean forbidDamaged = false;

    private IEMCProxy emcProxy;
    private ManagedEMCManager emcManager;

    private final EMCInventory emcInventory;

    public EMCItemHandler(@Nonnull UUID owner, @Nonnull World world)
    {
        this.owner = owner;
        this.world = world;

        this.emcProxy = ProjectEAPI.getEMCProxy();

        this.emcManager = EquivalentIntegrationsMod.emcManager;
        this.emcInventory = this.emcManager.getEMCInventory(owner);
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
        return this.emcInventory.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        ItemStack ret = this.emcInventory.getStackInSlot(slot);

        return ret;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        long startTime = System.nanoTime();

        if(slot < 0 || slot >= emcInventory.getSlots())
        {
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
                    emcManager.depositEMC(owner, emcValue);

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

                }

                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    public ItemStack extractItem(ItemStack desired, boolean simulate)
    {
        double emc = emcManager.getEMC(owner);

        long emcCost = emcManager.getEmcValue(desired);

        if(emcCost == 0)
        {
            return ItemStack.EMPTY;
        }

        int actualAmount = desired.getCount();

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

        //if that happens, rather than trying to solve this calculus, just round it off
        if(desiredEMC > emc)
        {
            desiredEMC = (long)emc;
        }

        if (!simulate && desiredEMC > 0)
        {
            emcManager.withdrawEMC(owner, desiredEMC);
        }

        return ret;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(slot < 0 || slot >= emcInventory.realSize())
        {
            return ItemStack.EMPTY;
        }

        //first off, what's in this stack?
        ItemStack desired = emcInventory.getStackInSlot(slot);

        if(desired.getCount() != amount)
        {
            desired = ItemHandlerHelper.copyStackWithSize(desired, amount);
        }

        return extractItem(desired, simulate);
    }

    private int getEfficiencyCost(ItemStack stack, long emcCost)
    {
        if(emcCost < efficiencyThreshold) return 0;

        return stack.getCount();
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return emcInventory.getSlotLimit(slot);
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

    public Collection<ItemStack> getCachedInventory()
    {
        return emcInventory.getCachedInventory();
    }

    public boolean getForbidNbt()
    {
        return forbidNbt;
    }

    public void setForbidNbt(boolean forbidNbt)
    {
        this.forbidNbt = forbidNbt;
    }

    public boolean getForbidDamaged()
    {
        return forbidDamaged;
    }

    public void setForbidDamaged(boolean forbidDamaged)
    {
        this.forbidDamaged = forbidDamaged;
    }
}
