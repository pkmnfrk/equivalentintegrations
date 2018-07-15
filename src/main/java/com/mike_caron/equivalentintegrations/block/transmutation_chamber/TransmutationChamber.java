package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.TransmutationBlockBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class TransmutationChamber extends TransmutationBlockBase implements ITileEntityProvider
{
    public static final String id = "transmutation_chamber";
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public static final int GUI_ID = 1;

    public TransmutationChamber() {
        super(id);

        setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE,false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TransmutationChamberTileEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TransmutationChamberTileEntity tileEntity = getTE(worldIn, pos);
        if(tileEntity == null) return getDefaultState();

        return state.withProperty(ACTIVE, tileEntity.hasOwner());
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ACTIVE);
    }

    @Nullable
    private TransmutationChamberTileEntity getTE(IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity ret = worldIn.getTileEntity(pos);
        if(ret instanceof TransmutationChamberTileEntity) return (TransmutationChamberTileEntity)ret;
        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote) return true;

        TransmutationChamberTileEntity te = getTE(worldIn, pos);

        if(te == null) return false;

        playerIn.openGui(EquivalentIntegrationsMod.instance, GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }
}
