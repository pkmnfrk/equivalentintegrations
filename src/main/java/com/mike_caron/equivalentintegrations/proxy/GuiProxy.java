package com.mike_caron.equivalentintegrations.proxy;

import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberContainer;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberContainerGui;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.equivalentintegrations.block.transmutation_generator.TransmutationGeneratorContainer;
import com.mike_caron.equivalentintegrations.block.transmutation_generator.TransmutationGeneratorContainerGui;
import com.mike_caron.equivalentintegrations.block.transmutation_generator.TransmutationGeneratorTileEntity;
import com.mike_caron.equivalentintegrations.item.ConjurationAssembler;
import com.mike_caron.equivalentintegrations.item.ConjurationAssemblerContainer;
import com.mike_caron.equivalentintegrations.item.ConjurationAssemblerContainerGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiProxy implements IGuiHandler
{
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if(ID == ConjurationAssemblerContainer.GUI_ID)
        {
            return createConjurationContainer(player, x);
        }
        else
        {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof TransmutationChamberTileEntity)
            {
                return new TransmutationChamberContainer(player.inventory, (TransmutationChamberTileEntity) te);
            }
            else if (te instanceof TransmutationGeneratorTileEntity)
            {
                return new TransmutationGeneratorContainer(player.inventory, (TransmutationGeneratorTileEntity) te);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if(ID == ConjurationAssemblerContainer.GUI_ID)
        {
            return new ConjurationAssemblerContainerGui(createConjurationContainer(player, x));
        }
        else
        {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof TransmutationChamberTileEntity)
            {
                TransmutationChamberTileEntity TCte = (TransmutationChamberTileEntity) te;
                return new TransmutationChamberContainerGui(TCte, new TransmutationChamberContainer(player.inventory, TCte));
            }
            else if (te instanceof TransmutationGeneratorTileEntity)
            {
                TransmutationGeneratorTileEntity TGte = (TransmutationGeneratorTileEntity) te;
                return new TransmutationGeneratorContainerGui(TGte, new TransmutationGeneratorContainer(player.inventory, TGte));
            }
        }
        return null;
    }

    @Nonnull
    private static ConjurationAssemblerContainer createConjurationContainer(EntityPlayer player, int slot)
    {
        return new ConjurationAssemblerContainer(player.inventory, new ConjurationAssembler.Inventory(player, slot), slot);
    }
}
