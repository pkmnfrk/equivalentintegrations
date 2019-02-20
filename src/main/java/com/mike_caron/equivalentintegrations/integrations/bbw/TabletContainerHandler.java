package com.mike_caron.equivalentintegrations.integrations.bbw;


import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.gameObjs.items.TransmutationTablet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.api.IContainerHandler;

public class TabletContainerHandler
    implements IContainerHandler
{
    @Override
    public boolean matches(EntityPlayer entityPlayer, ItemStack itemStack, ItemStack inventoryStack)
    {
        if(inventoryStack.getItem() instanceof TransmutationTablet)
        {
            IKnowledgeProvider knowledge = ProjectEAPI.getTransmutationProxy().getKnowledgeProviderFor(entityPlayer.getUniqueID());

            if(knowledge.hasKnowledge(itemStack))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int countItems(EntityPlayer entityPlayer, ItemStack itemStack, ItemStack inventoryStack)
    {
        if(inventoryStack.getItem() instanceof TransmutationTablet)
        {
            EMCItemHandler handler = new EMCItemHandler(entityPlayer.getUniqueID(), entityPlayer.world, true, true, itemStack);

            for(ItemStack stack : handler.getCachedInventory())
            {
                return stack.getCount();
            }
        }
        return 0;
    }

    @Override
    public int useItems(EntityPlayer entityPlayer, ItemStack itemStack, ItemStack inventoryStack, int num)
    {
        if(inventoryStack.getItem() instanceof TransmutationTablet)
        {
            EMCItemHandler handler = new EMCItemHandler(entityPlayer.getUniqueID(), entityPlayer.world, true, true, itemStack);

            ItemStack extrack = itemStack.copy();
            extrack.setCount(num);

            ItemStack result = handler.extractItem(extrack, false);

            return num - result.getCount();
        }
        return 0;
    }

    public static void register()
    {
        BetterBuildersWandsMod.instance.containerManager.register(new TabletContainerHandler());
    }
}
