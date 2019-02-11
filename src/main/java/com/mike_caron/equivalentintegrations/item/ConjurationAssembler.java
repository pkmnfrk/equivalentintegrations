package com.mike_caron.equivalentintegrations.item;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.client.renderer.item.ConjurationAssemblerModel;
import com.mike_caron.equivalentintegrations.inventory.IInventoryCallback;
import com.mike_caron.equivalentintegrations.inventory.ItemInventory;
import com.mike_caron.equivalentintegrations.inventory.PlayerItemInventory;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import com.mike_caron.equivalentintegrations.util.ItemUtils;
import com.mike_caron.equivalentintegrations.util.MappedModelLoader;
import com.mike_caron.equivalentintegrations.util.OptionalInt;
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
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ConjurationAssembler
    extends ItemBase
    implements IItemConfig
{
    public static final String id = "conjuration_assembler";

    public static int STACK_LIMIT = 1;
    public static final int NESTED_ITEM_TINT_DELTA = 1;

    private static final class Data
    {

        @Nullable
        public final UUID playerUuid;
        @Nonnull
        public final ItemStack itemStack;
        public final int color;
        public Data(@Nullable UUID playerUuid, @Nonnull ItemStack itemStack, @Nonnull int color)
        {
            this.playerUuid = playerUuid;
            this.itemStack = itemStack;
            this.color = color;
        }

    }
    private static final LoadingCache<ItemStack, Data> itemCache = CacheBuilder
        .newBuilder()
        .softValues()
        .expireAfterAccess(10, TimeUnit.SECONDS)
        .build(new CacheLoader<ItemStack, Data>()
        {
            @Override
            @Nonnull
            public Data load(@Nonnull ItemStack key) throws Exception
            {
                return getData(key);
            }
        });

    public ConjurationAssembler()
    {
        setRegistryName(id);
        setTranslationKey(id);
        setMaxStackSize(1);

        this.addPropertyOverride(new ResourceLocation("active"), (stack, worldIn, entityIn) -> {
            final Data data = itemCache.getUnchecked(stack);
            boolean isActive = data.playerUuid != null && !data.itemStack.isEmpty();
            return isActive ? 1F : 0F;
        });

        this.addPropertyOverride(new ResourceLocation("color"), (stack, worldIn, entityIn) -> {
            final Data data = itemCache.getUnchecked(stack);
            return data.color;
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

        /*
        ItemStack held = getFilter(stack);
        if(held.isEmpty())
        {
            tip = I18n.format("item.conjuration_assembler.desc2x");
        }
        else
        {
            tip = I18n.format("item.conjuration_assembler.desc2", held.getDisplayName());
        }

        tooltip.add(tip);
        */
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack)
    {
        String base = super.getItemStackDisplayName(stack);

        Data held = itemCache.getUnchecked(stack);
        if(!held.itemStack.isEmpty())
            base += " (" + held.itemStack.getDisplayName() + ")";

        return base;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        final Data data = itemCache.getUnchecked(stack);

        if(hand != EnumHand.MAIN_HAND)
        {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }

        if(!world.isRemote && (player.isSneaking()))
        {
            if(!player.getUniqueID().equals(data.playerUuid))
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
            final Data data = itemCache.getUnchecked(containerStack);

            if (data.playerUuid != null && !data.itemStack.isEmpty())
            {
                EMCItemHandler handler = getItemHandler(data.playerUuid, worldIn, data.itemStack);

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
        final Data data = itemCache.getUnchecked(container);
        return data.itemStack;
    }

    @Nullable
    private static UUID getPlayerUUID(final @Nonnull ItemStack container)
    {
        final Data data = itemCache.getUnchecked(container);
        return data.playerUuid;
    }

    @Nonnull
    private static Data getData(final @Nonnull ItemStack container)
    {
        UUID uuid = null;
        ItemStack stack = ItemStack.EMPTY;
        int color = 9;

        if(!container.isEmpty())
        {
            NBTTagCompound compound = container.getTagCompound();

            if (compound != null && compound.hasKey("player"))
            {
                uuid = UUID.fromString(compound.getString("player"));
            }

            if(compound != null && compound.hasKey("color"))
            {
                color = compound.getInteger("color");
            }

            ItemInventory inv = new ItemInventory(container, 1);
            stack = inv.getStackInSlot(0);
        }

        return new Data(uuid, stack, color);
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
        return withOwnerAndFilter(owner, ItemStack.EMPTY, 9);
    }

    @Nonnull
    public static ItemStack withOwner(@Nonnull ItemStack original, @Nonnull UUID owner)
    {
        final Data data = itemCache.getUnchecked(original);
        return withOwnerAndFilter(owner, data.itemStack, data.color);
    }

    @Nonnull
    public static ItemStack withFilter(@Nonnull ItemStack original, @Nonnull ItemStack filter)
    {
        final Data data = itemCache.getUnchecked(original);
        return withOwnerAndFilter(data.playerUuid, filter, data.color);
    }

    @Nonnull
    public static ItemStack withOwnerAndFilter(@Nullable UUID owner, @Nonnull ItemStack filter, int color)
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

        tag.setInteger("color", color);


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
                final Data data = itemCache.getUnchecked(stack);
                if (!data.itemStack.isEmpty()) {
                    final boolean isMatching = data.itemStack.isItemEqualIgnoreDurability(pickedStack);
                    if (isMatching) {

                        EMCItemHandler handler = getItemHandler(data.playerUuid, evt.getItem().world, data.itemStack);
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

        public int getCurrentColor()
        {
            NBTTagCompound compound = ItemUtils.getItemTag(containerStack);
            if(!compound.hasKey("color"))
                return 9;
            return compound.getInteger("color");
        }
    }

    @Override
    public void onConfig(@Nonnull ItemStack stack, int discriminator, int payload)
    {
        NBTTagCompound compound = ItemUtils.getItemTag(stack);
        switch(discriminator)
        {
            case 1:
                compound.setInteger("color", payload);
        }
    }
}
