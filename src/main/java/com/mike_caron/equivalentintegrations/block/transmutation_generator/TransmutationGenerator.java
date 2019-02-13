package com.mike_caron.equivalentintegrations.block.transmutation_generator;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.TransmutationBlockBase;
import com.mike_caron.equivalentintegrations.block.TransmutationTileEntityBase;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TransmutationGenerator
        extends TransmutationBlockBase
{
    public static final String id = "transmutation_generator";

    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    public static final PropertyBool GENERATING = PropertyBool.create("generating");

    public static final int GUI_ID = 2;
    public TransmutationGenerator()
    {
        super(id);

        setDefaultState(
                this.blockState.getBaseState()
                .withProperty(ACTIVE, false)
                .withProperty(GENERATING, false)
        );
    }

    @Nonnull
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TransmutationGeneratorTileEntity();
    }

    @Override
    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos)
    {
        TransmutationGeneratorTileEntity te = getTE(world, observerPos);
        if(te != null)
        {
            te.updateNeighbours();
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        TransmutationGeneratorTileEntity te = getTE(world, pos);
        if(te != null)
        {
            te.updateNeighbours();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    @Nonnull
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TransmutationGeneratorTileEntity tileEntity = getTE(worldIn, pos);
        if(tileEntity == null) return getDefaultState();

        return state
                .withProperty(ACTIVE, tileEntity.hasOwner())
                .withProperty(GENERATING, tileEntity.isGenerating());
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ACTIVE, GENERATING);
    }

    @Nullable
    private TransmutationGeneratorTileEntity getTE(IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity ret = worldIn.getTileEntity(pos);
        if(ret instanceof TransmutationGeneratorTileEntity) return (TransmutationGeneratorTileEntity)ret;
        return null;
    }



    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote) return true;

        TransmutationGeneratorTileEntity te = getTE(worldIn, pos);

        if(te == null) return false;

        playerIn.openGui(EquivalentIntegrationsMod.instance, GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        List<String> ret =  super.getWailaBody(itemStack, currenttip, accessor, config);

        TileEntity te = accessor.getTileEntity();
        TransmutationGeneratorTileEntity tileEntity = null;
        if(te instanceof TransmutationTileEntityBase)
        {
            tileEntity = (TransmutationGeneratorTileEntity)te;
        }

        if(tileEntity != null)
        {
            ret.add(getProducesString(tileEntity));
            ret.add(getConsumesString(tileEntity));
        }

        return ret;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);

        TransmutationGeneratorTileEntity te = getTE(world, data.getPos());

        if(te == null) return;

        probeInfo
                .vertical()
                .text(getProducesString(te))
//                .horizontal()
                .text(getConsumesString(te))
                ;
    }

    private String getProducesString(TransmutationGeneratorTileEntity tileEntity)
    {
        StringBuilder ret = new StringBuilder();

        ret
            .append(new TextComponentTranslation("term.equivalentintegrations.produces").getFormattedText())
            .append(" ")
            .append(tileEntity.getPowerPerTick())
            .append(" ")
            .append(new TextComponentTranslation("term.equivalentintegrations.fept").getFormattedText())
        ;

        return ret.toString();

    }

    private String getConsumesString(TransmutationGeneratorTileEntity tileEntity)
    {
        StringBuilder ret = new StringBuilder();

        ret
                .append(new TextComponentTranslation("term.equivalentintegrations.consumes").getFormattedText())
                .append(" ")
                .append(tileEntity.getEMCPerTick())
                .append(" ")
                .append(new TextComponentTranslation("term.equivalentintegrations.emcpt").getFormattedText())
        ;

        return ret.toString();

    }
}
