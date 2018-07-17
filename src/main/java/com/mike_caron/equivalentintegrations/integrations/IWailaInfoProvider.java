package com.mike_caron.equivalentintegrations.integrations;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IWailaInfoProvider
{
    List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config);
}
