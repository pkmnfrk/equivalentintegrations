package com.mike_caron.equivalentintegrations.impl;

import com.mike_caron.equivalentintegrations.api.capabilities.IEMCManager;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EMCManagerProvider implements ICapabilityProvider
{
    @CapabilityInject(IEMCManager.class)
    public static final Capability<IEMCManager> EMC_MANAGER_CAPABILITY = null;

    private IEMCManager instance = EMC_MANAGER_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == EMC_MANAGER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == EMC_MANAGER_CAPABILITY)
        {
            return EMC_MANAGER_CAPABILITY.cast(instance);
        }

        return null;
    }
}
