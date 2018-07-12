package com.mike_caron.equivalentintegrations.block;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TransmutationChamber extends Block implements ITileEntityProvider
{
    public static final String id = "transmutationChamber";
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public TransmutationChamber() {
        super(Material.ROCK);
        setRegistryName(id);
        setUnlocalizedName(id);

        setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE,false));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TransmutationChamberTileEntity();
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state.withProperty(ACTIVE, getTE(worldIn, pos).hasOwner());
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(ACTIVE, false), 2);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ACTIVE);
    }

    private TransmutationChamberTileEntity getTE(IBlockAccess worldIn, BlockPos pos)
    {
        return (TransmutationChamberTileEntity)worldIn.getTileEntity(pos);
    }
}
