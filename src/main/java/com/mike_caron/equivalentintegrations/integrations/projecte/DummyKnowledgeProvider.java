package com.mike_caron.equivalentintegrations.integrations.projecte;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class DummyKnowledgeProvider
    implements IKnowledgeProvider
{
    @Override
    public boolean hasFullKnowledge()
    {
        return false;
    }

    @Override
    public void setFullKnowledge(boolean b)
    {

    }

    @Override
    public void clearKnowledge()
    {

    }

    @Override
    public boolean hasKnowledge(@Nonnull ItemStack itemStack)
    {
        return false;
    }

    @Override
    public boolean addKnowledge(@Nonnull ItemStack itemStack)
    {
        return false;
    }

    @Override
    public boolean removeKnowledge(@Nonnull ItemStack itemStack)
    {
        return false;
    }

    @Nonnull
    @Override
    public List<ItemStack> getKnowledge()
    {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public IItemHandler getInputAndLocks()
    {
        return new IItemHandler()
        {
            @Override
            public int getSlots()
            {
                return 0;
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int i)
            {
                return ItemStack.EMPTY;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean b)
            {
                return itemStack;
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int i, int i1, boolean b)
            {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int i)
            {
                return 0;
            }
        };
    }

    @Override
    public long getEmc()
    {
        return 0;
    }

    @Override
    public void setEmc(long l)
    {

    }

    @Override
    public void sync(@Nonnull EntityPlayerMP entityPlayerMP)
    {

    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound)
    {

    }
}
