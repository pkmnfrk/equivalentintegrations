package com.mike_caron.equivalentintegrations.proxy;

import com.mike_caron.equivalentintegrations.block.TransmutationChamberContainer;
import com.mike_caron.equivalentintegrations.block.TransmutationChamberContainerGui;
import com.mike_caron.equivalentintegrations.block.TransmutationChamberTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiProxy implements IGuiHandler
{
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x,y,z);
        TileEntity te = world.getTileEntity(pos);

        if(te instanceof TransmutationChamberTileEntity) {
            return new TransmutationChamberContainer(player.inventory, (TransmutationChamberTileEntity)te);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x,y,z);
        TileEntity te = world.getTileEntity(pos);

        if(te instanceof TransmutationChamberTileEntity) {
            TransmutationChamberTileEntity TCte = (TransmutationChamberTileEntity)te;
            return new TransmutationChamberContainerGui(TCte, new TransmutationChamberContainer(player.inventory, TCte));
        }

        return null;
    }
}
