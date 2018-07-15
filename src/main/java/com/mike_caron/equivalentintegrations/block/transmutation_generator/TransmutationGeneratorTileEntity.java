package com.mike_caron.equivalentintegrations.block.transmutation_generator;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.TransmutationTileEntityBase;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberItemStackHandler;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.equivalentintegrations.item.SoulboundTalisman;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TransmutationGeneratorTileEntity
        extends TransmutationTileEntityBase
{
    @Override
    @Nonnull
    protected ItemStackHandler createInventory()
    {
        return new TransmutationGeneratorStackHandler()
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                if(world.isRemote)
                    return;

                ItemStack stack = this.getStackInSlot(0);
                UUID owner = SoulboundTalisman.getOwnerFromStack(stack);

                TransmutationGeneratorTileEntity.this.setOwner(owner);
                TransmutationGeneratorTileEntity.this.markDirty();

                //TransmutationGeneratorTileEntity.this.setTransmutationParameters();
            }
        };
    }

    @Override
    protected void onNewOwner()
    {
        //todo: something here, probably
    }

    public boolean isGenerating()
    {
        return false;
    }

    private float getEfficiency()
    {
        ItemStack stack = inventory.getStackInSlot(1);

        return getEfficiency(stack.getCount());
    }

    public static float getEfficiency(int num)
    {
        switch(num)
        {
            case 0: return 0.5f;
            case 1: return 1f;
            case 2: return 2f;
            default: return 3f;
        }
    }
}
