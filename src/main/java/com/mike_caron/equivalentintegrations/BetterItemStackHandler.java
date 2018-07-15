package com.mike_caron.equivalentintegrations;

import net.minecraft.nbt.NBTTagCompound;
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
        super.setSize(size);
        explicitSize = size;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);

        if(explicitSize >= 0) {
            this.setSize(explicitSize);
        }
    }
}
