package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.client.renderer.item.ConjurationAssemblerModel;
import com.mike_caron.equivalentintegrations.inventory.ItemInventory;
import com.mike_caron.equivalentintegrations.inventory.PlayerItemInventory;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import com.mike_caron.equivalentintegrations.util.MappedModelLoader;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ConjurationAssembler extends ItemBase
{
    public static final String id = "conjuration_assembler";

    public static int STACK_LIMIT = 1;
    public static final int NESTED_ITEM_TINT_DELTA = 1;

    public ConjurationAssembler()
    {
        setRegistryName(id);
        setTranslationKey(id);
        setMaxStackSize(1);

        this.addPropertyOverride(new ResourceLocation("active"), new IItemPropertyGetter()
        {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                boolean isActive = getPlayerUUID(stack) != null && !getFilter(stack).isEmpty();
                return isActive ? 1F : 0F;
            }
        });
    }

    @Override
    public void initModel(MappedModelLoader.Builder models)
    {
        //models.put("magic-" + id, ConjurationAssemblerModel.INSTANCE);
        super.initModel(models);
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
        ItemStack stack = player.getHeldItem(hand);

        if(hand != EnumHand.MAIN_HAND)
        {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }

        if(!world.isRemote && (player.isSneaking()))
        {
            if(!player.getUniqueID().equals(getPlayerUUID(stack)))
            {
                stack = withOwner(stack, player.getUniqueID());
                player.setHeldItem(hand, stack);
            }

            player.openGui(EquivalentIntegrationsMod.instance, ConjurationAssemblerContainer.GUI_ID, world, player.inventory.currentItem, 0, 0);
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(!worldIn.isRemote)
        {
            final ItemStack containerStack = player.getHeldItem(EnumHand.MAIN_HAND);
            final ItemStack filter = getFilter(containerStack);
            final UUID playerUuid = getPlayerUUID(containerStack);

            if (playerUuid != null && !filter.isEmpty())
            {
                EMCItemHandler handler = getItemHandler(playerUuid, worldIn, filter);

                ItemStack actualStack = handler.getStackInSlot(0).copy();
                if(!actualStack.isEmpty())
                {
                    if (actualStack.getCount() > 64)
                        actualStack.setCount(64);

                    player.setHeldItem(hand, actualStack.copy());
                    EnumActionResult result = actualStack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                    final ItemStack newActualStack = player.getHeldItem(hand);
                    player.setHeldItem(hand, containerStack);

                    ItemStack delta = getDelta(actualStack, newActualStack);

                    if (!delta.isEmpty())
                    {
                        ItemStack extractResult = handler.extractItem(delta, false);

                        if (extractResult.isEmpty())
                        {
                            EquivalentIntegrationsMod.logger.warn("Just leaked {}", delta);
                        }
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

    public static ItemStack getFilter(final @Nonnull ItemStack container)
    {
        if(container.isEmpty())
            return ItemStack.EMPTY;

        ItemInventory inv = new ItemInventory(container, 1);
        return inv.getStackInSlot(0);
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

    @Nonnull
    public static ItemStack withOwner(@Nonnull UUID owner)
    {
        return withOwnerAndFilter(owner, ItemStack.EMPTY);
    }

    @Nonnull
    public static ItemStack withOwner(@Nonnull ItemStack original, @Nonnull UUID owner)
    {
        ItemStack filter = getFilter(original);
        return withOwnerAndFilter(owner, filter);
    }

    @Nonnull
    public static ItemStack withFilter(@Nonnull ItemStack original, @Nonnull ItemStack filter)
    {
        UUID owner = getPlayerUUID(original);
        return withOwnerAndFilter(owner, filter);
    }

    @Nonnull
    public static ItemStack withOwnerAndFilter(@Nullable UUID owner, @Nonnull ItemStack filter)
    {
        NBTTagCompound tag = new NBTTagCompound();

        if(owner != null)
        {
            tag.setString("player", owner.toString());
        }

        if(!filter.isEmpty())
        {
            tag.setTag(ItemInventory.TAG_INVENTORY, filter.serializeNBT());
        }


        ItemStack ret =  new ItemStack(ModItems.conjurationAssembler, 1);
        ret.setTagCompound(tag);

        ItemInventory inv = new ItemInventory(ret, 1);
        inv.setInventorySlotContents(0, filter);

        return ret;
    }

    @SubscribeEvent
    public void onItemPickUp(EntityItemPickupEvent evt) {

        final EntityPlayer player = evt.getEntityPlayer();
        final ItemStack pickedStack = evt.getItem().getItem();

        if (pickedStack.isEmpty() || player == null) return;

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            final ItemStack stack = player.inventory.getStackInSlot(i);

            if (stack.getItem() == this) {
                final ItemStack containedStack = getFilter(stack);
                final UUID playerUuid = getPlayerUUID(stack);
                if (!containedStack.isEmpty()) {
                    final boolean isMatching = containedStack.isItemEqualIgnoreDurability(pickedStack);
                    if (isMatching) {

                        EMCItemHandler handler = getItemHandler(playerUuid, evt.getItem().world, containedStack);
                        ItemStack result = handler.insertItem(0, pickedStack, false);
                        if(result.getCount() != pickedStack.getCount())
                        {
                            if(pickedStack.getCount() > 0)
                                pickedStack.setCount(0);
                            return;
                        }
                    }
                }
            }
        }
    }

    public static class Inventory
        extends PlayerItemInventory
    {
        public Inventory(EntityPlayer player)
        {
            super(player, 1);
        }

        public Inventory(EntityPlayer player, int inventorySlot)
        {
            super(player, 1, inventorySlot);
        }


    }
}
