package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.mikesmodslib.item.ItemBase;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BuilderBag
    extends ItemBase
{
    public static final String id = "builder_bag";

    public BuilderBag()
    {
        setRegistryName(id);
        setTranslationKey(id);
        setMaxStackSize(1);
        setCreativeTab(EquivalentIntegrationsMod.creativeTab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        String tip;

        tip = I18n.format("item.builder_bag.desc1");
        if(!tip.isEmpty())
        {
            tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + tip);
        }
    }

    @SubscribeEvent
    public void onItemPickUp(EntityItemPickupEvent evt) {

        final EntityPlayer player = evt.getEntityPlayer();
        final ItemStack pickedStack = evt.getItem().getItem();

        if (pickedStack.isEmpty() || player == null) return;

        if(!(pickedStack.getItem() instanceof BuilderBag)) return;

        String uuid = player.getUniqueID().toString();
        NBTTagCompound nbt = ItemUtils.getItemTag(pickedStack);
        nbt.setString("player", uuid);
        pickedStack.setTagCompound(nbt);

    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        return super.initCapabilities(stack, nbt);
    }

    static class Provider
        implements ICapabilityProvider
    {
        ItemStack self;

        public Provider(ItemStack stack)
        {
            this.self = stack;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
        {
            //if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            //    return true;
            return false;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
        {
            //UUID playerUuid = ItemStackUtils.getPlayerUUID(self);
            //EntityPlayer player =
            //World world = player.getWorld();

            //return new EMCItemHandler(playerUuid, world);
            return null;
        }
    }
}
