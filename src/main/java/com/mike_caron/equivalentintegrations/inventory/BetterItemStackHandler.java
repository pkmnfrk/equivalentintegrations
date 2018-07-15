package com.mike_caron.equivalentintegrations.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class BetterItemStackHandler extends ItemStackHandler
{
    private int explicitSize = -1;

    public BetterItemStackHandler()
    {
        super();
    }

    public BetterItemStackHandler(int size)
    {
        super(size);
        explicitSize = size;
    }

    @Override
    public void setSize(int size)
    {

    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        //since setSize is neutered, we need to do this part ourselves
        int size = nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size();
        if(size > explicitSize) {
            explicitSize = size;
        }

        stacks = NonNullList.withSize(explicitSize, ItemStack.EMPTY);

        super.deserializeNBT(nbt);

    }
}
