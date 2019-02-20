package com.mike_caron.equivalentintegrations.storage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.impl.ManagedEMCManager;
import com.mike_caron.mikesmodslib.util.LastResortUtils;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.NBTWhitelist;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

//import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;

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

    private boolean canImport;
    private boolean canExport;

    private IEMCProxy emcProxy;
    private ManagedEMCManager emcManager;

    private final EMCInventory emcInventory;

    public EMCItemHandler(@Nonnull UUID owner, @Nonnull World world, boolean canImport, boolean canExport)
    {
        this(owner, world, canImport, canExport, null);
    }

    public EMCItemHandler(@Nonnull UUID owner, @Nonnull World world, boolean canImport, boolean canExport, @Nullable ItemStack filter)
    {
        this.owner = owner;
        this.world = world;
        this.canImport = canImport;
        this.canExport = canExport;

        this.emcProxy = ProjectEAPI.getEMCProxy();

        this.emcManager = EquivalentIntegrationsMod.emcManager;

        EMCInventory inv = this.emcManager.getEMCInventory(owner);
        if(filter != null)
            inv = inv.withFilter(filter);

        this.emcInventory = inv;
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
        if(!canExport)
            return ItemStack.EMPTY;

        ItemStack ret = this.emcInventory.getStackInSlot(slot);

        return ret;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if(!canImport)
            return stack;

        long startTime = System.nanoTime();

        if(slot < 0 || slot >= emcInventory.getSlots())
        {
            return stack;
        }

        if(emcProxy.hasValue(stack)){
            IKnowledgeProvider knowledge;

            knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(owner);

            if(forbidNbt && stack.hasTagCompound() && !(stack.getTagCompound().getSize() == 0))
            {
                return stack;
            }

            if(forbidDamaged && stack.isItemDamaged())
            {
                return stack;
            }

            if(canLearn || knowledge.hasKnowledge(stack))
            {
                double emc = emcManager.getEMC(owner);

                long singleValue = emcManager.getEmcSellValue(stack);

                long emcValue = singleValue * stack.getCount();

                emcValue -= getEfficiencyCost(stack, emcValue);

                //EquivalentIntegrationsMod.logger.info("Burning a stack ({}) for {} EMC each, a total of {} (Simulation: {})", stack, singleValue, emcValue, simulate);

                if(!knowledge.hasKnowledge(stack))
                {
                    if(!tryLearn(stack, knowledge, simulate))
                    {
                        return stack;
                    }
                }

                if(!simulate)
                {
                    emcManager.depositEMC(owner, emcValue);
                }

                //EquivalentIntegrationsMod.logger.info("Done burning said stack");

                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    private boolean tryLearn(@Nonnull ItemStack stack, IKnowledgeProvider knowledge, boolean simulate)
    {
        if(!simulate)
        {
            stack = stack.copy();

            //then, clean up the stack a bit
            if (ItemHelper.isDamageable(stack))
            {
                stack.setItemDamage(0);
            }

            stack.setCount(1);
            if (stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack))
            {
                stack.setTagCompound(null);
            }
        }

        EntityPlayer player = world.getPlayerEntityByUUID(owner);
        if (player != null && !MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, stack)))
        {
            //note: this will not work if the user is offline. In this case, the later
            //knowledge check will return false, thus rejecting the item

            return simulate || knowledge.addKnowledge(stack);
        }

        return false;
    }

    public ItemStack extractItem(ItemStack desired, boolean simulate)
    {
        if(!canExport)
            return ItemStack.EMPTY;

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

        //EquivalentIntegrationsMod.logger.info("Materializing a stack ({}) for {} EMC each, a total of {} (Simulation: {})", ret, emcCost, desiredEMC, simulate);

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

        //EquivalentIntegrationsMod.logger.info("Done materializing said stack");

        return ret;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(!canExport)
            return ItemStack.EMPTY;
        
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

    private static boolean isPlayerOnline(UUID player)
    {
        return LastResortUtils.getPlayer(player) != null;
    }
}
