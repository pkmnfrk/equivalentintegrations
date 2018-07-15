package com.mike_caron.equivalentintegrations.block.transmutation_generator;

import com.mike_caron.equivalentintegrations.item.ModItems;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TransmutationGeneratorStackHandler extends ItemStackHandler
{
    public static final int NUM_SLOTS = 2;

    public TransmutationGeneratorStackHandler()
    {
        super(NUM_SLOTS);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        setSize(NUM_SLOTS);
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack)
    {
        if(slot == 0 && stack.getItem() == ModItems.soulboundTalisman && SoulboundTalisman.isBound(stack))
        {
            return 1;
        }
        else if(slot == 1 && stack.getItem() == ModItems.efficiencyCatalyst)
        {
            return 4;
        }

        return 0;
    }
}
