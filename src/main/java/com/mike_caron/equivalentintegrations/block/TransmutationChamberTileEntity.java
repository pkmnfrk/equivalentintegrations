package com.mike_caron.equivalentintegrations.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TransmutationChamberTileEntity extends TileEntity
{
    private UUID owner = null;

    public void setOwner(UUID newOwner)
    {
        this.owner = owner;
        this.markDirty();
    }

    public boolean hasOwner()
    {
        return owner != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey("owner"))
        {
            owner = compound.getUniqueId("owner");
        }
        else
        {
            owner = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if(owner != null)
        {
            compound.setUniqueId("owner", owner);
        }
        else
        {
            if(compound.hasKey("owner")) {
                compound.removeTag("owner");
            }
        }

        return compound;
    }
}
