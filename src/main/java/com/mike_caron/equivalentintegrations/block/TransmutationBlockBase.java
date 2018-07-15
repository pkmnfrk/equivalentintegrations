package com.mike_caron.equivalentintegrations.block;

import com.mike_caron.equivalentintegrations.block.transmutation_generator.TransmutationGeneratorTileEntity;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TransmutationBlockBase
        extends BlockBase
        implements ITileEntityProvider
{
    public TransmutationBlockBase(String id)
    {
        super(Material.IRON);
        setHardness(4);
        setRegistryName(id);
        setUnlocalizedName(id);
        setHarvestLevel("pickaxe", 1);


    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, getDefaultState(), 2);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    @Nonnull
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        if(!worldIn.isRemote)
        {
            TransmutationTileEntityBase te = getTE(worldIn, pos);
            ItemStackHandler inventory = te.getInventory();

            for (int i = 0; i < inventory.getSlots(); ++i)
            {
                ItemStack itemstack = inventory.getStackInSlot(i);

                if (!itemstack.isEmpty())
                {
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                }
            }
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Nullable
    private TransmutationTileEntityBase getTE(IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity ret = worldIn.getTileEntity(pos);
        if(ret instanceof TransmutationTileEntityBase) return (TransmutationTileEntityBase)ret;
        return null;
    }

}
