package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.proxy.IConversionProxy;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import javax.xml.soap.Text;
import java.util.List;
import java.util.UUID;


public class SoulboundTalisman extends Item
{
    public static final String id = "soulbound_talisman";
    public static final String OWNER_UUID = "OwnerUUID";
    public static final String OWNER_NAME = "OwnerName";

    public SoulboundTalisman()
    {
        setRegistryName(id);
        setUnlocalizedName(id);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack talisman = playerIn.getHeldItem(handIn);

        if(worldIn.isRemote || isBound(talisman))
            return new ActionResult<ItemStack>(EnumActionResult.PASS, talisman);

        NBTTagCompound nbt;

        if(talisman.hasTagCompound()) {
            nbt = talisman.getTagCompound();
        }
        else
        {
            nbt = new NBTTagCompound();
        }

        nbt.setString(OWNER_UUID, playerIn.getUniqueID().toString());
        nbt.setString(OWNER_NAME, playerIn.getDisplayNameString());

        talisman.setTagCompound(nbt);

        return new ActionResult<ItemStack>(EnumActionResult.PASS, talisman);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if(stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();

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

                ITransmutationProxy proxy = ProjectEAPI.getTransmutationProxy();
                IKnowledgeProvider provider = proxy.getKnowledgeProviderFor(UUID.fromString(uuid));
            }
        }
        tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.soulbound_talisman.desc1"));
        tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.soulbound_talisman.desc2"));



    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
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

        if(!nbt.hasKey(OWNER_UUID)) return false;
        String owner = nbt.getString(OWNER_UUID);

        return owner != null;
    }

    public static UUID getOwnerFromStack(ItemStack stack)
    {
        if(stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();

            if(nbt.hasKey(OWNER_UUID))
            {
                String uuid = nbt.getString(OWNER_UUID);
                return UUID.fromString(uuid);
            }
        }

        return null;
    }
}
