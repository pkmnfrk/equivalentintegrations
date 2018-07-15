package com.mike_caron.equivalentintegrations.proxy;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.api.capabilities.IEMCManager;
import com.mike_caron.equivalentintegrations.impl.ManagedEMCManager;
import moze_intel.projecte.utils.DummyIStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {

    @SuppressWarnings("EmptyMethod")
    public void preInit(FMLPreInitializationEvent e)
    {

    }

    public void init(FMLInitializationEvent e)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(EquivalentIntegrationsMod.instance, new GuiProxy());
        CapabilityManager.INSTANCE.register(IEMCManager.class, new DummyIStorage<>(), new ManagedEMCManager.Factory());
    }

    @SuppressWarnings("EmptyMethod")
    public void postInit(FMLPostInitializationEvent e)
    {

    }
}
