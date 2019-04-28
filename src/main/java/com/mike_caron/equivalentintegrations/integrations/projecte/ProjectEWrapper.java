package com.mike_caron.equivalentintegrations.integrations.projecte;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import moze_intel.projecte.utils.NBTWhitelist;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectEWrapper
{
    public final static ProjectEWrapper instance = getInstance();

    private ITransmutationProxy transmutationProxy;

    protected ProjectEWrapper() {
        transmutationProxy = ProjectEAPI.getTransmutationProxy();
    }

    private static ProjectEWrapper getInstance()
    {
        if(Loader.isModLoaded("projectex"))
        {
            ProjectEXWrapper wrapper = new ProjectEXWrapper();

            try
            {
                wrapper.test();
                return wrapper;
            }
            catch(NoClassDefFoundError er)
            {
                EquivalentIntegrationsMod.logger.error("Tried to instantiate ProjectEX API, but failed. Try updating Project-EX!", er);
            }

        }

        return new ProjectEWrapper();
    }

    public boolean isSafe()
    {
        return false;
    }

    public double getEmc(@Nonnull World world, @Nonnull UUID owner)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(world, owner);
        return provider.getEmc();
    }

    public double getEmc(@Nonnull EntityPlayer player)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(player);
        return provider.getEmc();
    }

    public void setEmc(@Nonnull World world, @Nonnull UUID owner, double emc)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(world, owner);
        provider.setEmc(emc);
    }

    public void setEmc(@Nonnull EntityPlayer player, double emc)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(player);
        provider.setEmc(emc);
    }

    public void sync(EntityPlayerMP playerMP)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(playerMP);
        provider.sync(playerMP);
    }

    public boolean hasKnowledge(@Nonnull World world, @Nonnull UUID owner, @Nonnull ItemStack stack)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(world, owner);
        return provider.hasKnowledge(stack);
    }

    public boolean hasKnowledge(@Nonnull EntityPlayer player, @Nonnull ItemStack stack)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(player);
        return provider.hasKnowledge(stack);
    }

    public boolean addKnowledge(@Nonnull World world, @Nonnull UUID owner, @Nonnull ItemStack stack)
    {
        IKnowledgeProvider provider = getKnowledgeProvider(world, owner);
        return provider.addKnowledge(stack);
    }

    public void cleanupKnowledge(EntityPlayerMP player)
    {
        IKnowledgeProvider knowledge;

        knowledge = getKnowledgeProvider(player);

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

        knowledge.sync(player);
    }

    protected IKnowledgeProvider getKnowledgeProvider(@Nonnull EntityPlayer player)
    {
        IKnowledgeProvider knowledge;

        knowledge = transmutationProxy.getKnowledgeProviderFor(player.getUniqueID());

        return knowledge;
    }

    protected IKnowledgeProvider getKnowledgeProvider(@Nonnull World world, @Nonnull UUID owner)
    {
        IKnowledgeProvider knowledge;

        knowledge = transmutationProxy.getKnowledgeProviderFor(owner);

        return knowledge;
    }

    public List<ItemStack> getKnowledge(@Nonnull World world, @Nonnull UUID owner)
    {
        IKnowledgeProvider knowledge;

        knowledge = transmutationProxy.getKnowledgeProviderFor(owner);

        return knowledge.getKnowledge();
    }
}
