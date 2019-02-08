package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.impl.ManagedEMCManager;
import com.mike_caron.equivalentintegrations.storage.EMCInventory;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.asm.transformers.ItemStackTransformer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.nsc.transform.patmat.Logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ConjurationAssembler extends ItemBase
{
    public static final String id = "conjuration_assembler";

    public ConjurationAssembler()
    {
        setRegistryName(id);
        setTranslationKey(id);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        String tip;

        tip = I18n.format("item.conjuration_assembler.desc1");
        if(!tip.isEmpty())
        {
            tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + tip);
        }
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
    {
        final ItemStack stack = player.getHeldItem(hand);

        if(hand != EnumHand.MAIN_HAND)
        {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }

        if(!world.isRemote && (player.isSneaking()))
        {
            //todo: open GUI
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(!worldIn.isRemote)
        {
            final ItemStack containerStack = player.getHeldItem(EnumHand.MAIN_HAND);
            final ItemStack contained = getContainedStack(containerStack);
            final UUID playerUuid = getPlayerUUID(containerStack);

            if (playerUuid != null && !contained.isEmpty())
            {
                EMCItemHandler handler = getItemHandler(playerUuid, worldIn, contained);

                ItemStack actualStack = handler.getStackInSlot(0);

                actualStack.setCount(64);

                player.setHeldItem(hand, actualStack);
                EnumActionResult result = actualStack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                final ItemStack newActualStack = player.getHeldItem(hand);
                player.setHeldItem(hand, containerStack);

                ItemStack delta = getDelta(actualStack, newActualStack);

                if(!delta.isEmpty())
                {
                    delta = handler.extractItem(delta, false);

                    if(!delta.isEmpty())
                    {
                        EquivalentIntegrationsMod.logger.warn("Just leaked {}", delta);
                    }
                }
            }
        }
        return EnumActionResult.PASS;
    }

    private static EMCItemHandler getItemHandler(UUID playerUuid, World worldIn, ItemStack filter)
    {
        EMCItemHandler handler = new EMCItemHandler(playerUuid, worldIn, filter);

        handler.setCanLearn(false);
        handler.setForbidDamaged(true);
        handler.setForbidNbt(true);
        handler.setEfficiencyThreshold(Integer.MAX_VALUE);

        return handler;
    }

    private static ItemStack getContainedStack(final @Nonnull ItemStack container)
    {
        if(container.isEmpty())
            return ItemStack.EMPTY;

        NBTTagCompound compound = container.getTagCompound();

        if(compound == null || !compound.hasKey("item"))
            return ItemStack.EMPTY;

        NBTTagCompound inv = compound.getCompoundTag("item");

        return new ItemStack(inv);
    }

    @Nullable
    private static UUID getPlayerUUID(final @Nonnull ItemStack container)
    {
        if(container.isEmpty())
            return null;

        NBTTagCompound compound = container.getTagCompound();

        if(compound == null || !compound.hasKey("player"))
            return null;

        String playerUuid = compound.getString("player");

        return UUID.fromString(playerUuid);
    }

    private static ItemStack getDelta(@Nonnull ItemStack before, @Nonnull ItemStack after)
    {
        if(before.isEmpty())
            throw new IllegalArgumentException("before cannot be empty");

        if(after.isEmpty())
            return before;

        if(!before.isItemEqualIgnoreDurability(after))
            throw new IllegalArgumentException("before doesn't match after");

        return new ItemStack(before.getItem(), before.getCount() - after.getCount(), before.getMetadata(), before.getTagCompound());
    }
}
