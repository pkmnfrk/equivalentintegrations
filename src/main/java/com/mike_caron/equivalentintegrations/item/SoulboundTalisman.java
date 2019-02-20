package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.ModConfig;
import com.mike_caron.mikesmodslib.item.ItemBase;
import com.mike_caron.mikesmodslib.util.ItemUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SoulboundTalisman extends ItemBase
{
    public static final String id = "soulbound_talisman";
    public static final String OWNER_UUID = "OwnerUUID";
    public static final String OWNER_NAME = "OwnerName";

    public SoulboundTalisman()
    {
        setRegistryName(id);
        setTranslationKey(id);
        setCreativeTab(EquivalentIntegrationsMod.creativeTab);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        //super.getSubItems(tab, items);
        if(this.isInCreativeTab(tab))
        {
            items.add(new ItemStack(this));
            if(ModConfig.exposeInvalidTalisman)
            {
                items.add(withOwner(UUID.randomUUID(), "A Stranger"));
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack talisman = playerIn.getHeldItem(handIn);

        if(worldIn.isRemote || isBound(talisman))
            return new ActionResult<>(EnumActionResult.PASS, talisman);

        NBTTagCompound nbt = ItemUtils.getItemTag(talisman);

        addOwner(nbt, playerIn.getUniqueID(), playerIn.getDisplayNameString());

        talisman.setTagCompound(nbt);

        return new ActionResult<>(EnumActionResult.PASS, talisman);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if(stack.hasTagCompound()) {
            NBTTagCompound nbt = Objects.requireNonNull(stack.getTagCompound());

            if(nbt.hasKey(OWNER_UUID))
            {
                String uuid = nbt.getString(OWNER_UUID);
                String playerName = TextFormatting.OBFUSCATED + "unknown";

                if(nbt.hasKey(OWNER_NAME)) {
                    playerName = nbt.getString(OWNER_NAME);
                }

                String tip = TextFormatting.BLUE + I18n.format("item.soulbound_talisman.bound", playerName);
                //tip = "Bound to " + nbt.getString(OWNER_UUID);
                tooltip.add(tip);
            }
        }
        tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.soulbound_talisman.desc1"));
        tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.soulbound_talisman.desc2"));



    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return isBound(stack);
    }

    public static boolean isBound(ItemStack stack)
    {
        if(stack == ItemStack.EMPTY) return false;

        if(stack.getItem() != ModItems.soulboundTalisman) return false;

        if(!stack.hasTagCompound()) return false;
        NBTTagCompound nbt = stack.getTagCompound();
        if(nbt == null) return false;

        return nbt.hasKey(OWNER_UUID);
    }

    @Nullable
    public static UUID getOwnerFromStack(ItemStack stack)
    {
        if(stack.hasTagCompound()) {
            NBTTagCompound nbt = Objects.requireNonNull(stack.getTagCompound());

            if(nbt.hasKey(OWNER_UUID))
            {
                String uuid = nbt.getString(OWNER_UUID);
                return UUID.fromString(uuid);
            }
        }

        return null;
    }

    @Nullable
    public static String getOwnerNameFromStack(ItemStack stack)
    {
        if(stack.hasTagCompound()) {
            NBTTagCompound nbt = Objects.requireNonNull(stack.getTagCompound());

            if(nbt.hasKey(OWNER_NAME))
            {
                return nbt.getString(OWNER_NAME);
            }
        }

        return null;
    }

    public static void addOwner(NBTTagCompound nbt, UUID uuid, String name)
    {
        nbt.setString(OWNER_UUID, uuid.toString());
        nbt.setString(OWNER_NAME, name);
    }

    public ItemStack withOwner(UUID uuid, String name)
    {
        ItemStack ret = new ItemStack(this);

        addOwner(ItemUtils.getItemTag(ret), uuid, name);

        return ret;
    }
}
