package com.mike_caron.equivalentintegrations.integrations.comcap;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.equivalentintegrations.storage.SlotlessEMCItemHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.cyclops.commoncapabilities.capability.itemhandler.SlotlessItemHandlerConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonCapabilitiesHandler
{
    private static boolean isLoaded = false;

    public static void register()
    {
        isLoaded = true;
    }

    public static void attachCapability(AttachCapabilitiesEvent<TileEntity> event)
    {
        if(!isLoaded) return;

        event.addCapability(new ResourceLocation(EquivalentIntegrationsMod.modId, "slotless_transmutation"), new Provider((TransmutationChamberTileEntity)event.getObject()));
    }

    static class Provider
        implements ICapabilityProvider
    {
        SlotlessEMCItemHandler itemHandler;

        public Provider(TransmutationChamberTileEntity tileEntity)
        {
            itemHandler = new SlotlessEMCItemHandler(tileEntity);
        }
        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing)
        {
            if(capability == SlotlessItemHandlerConfig.CAPABILITY)
                return true;
            return false;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing)
        {
            if(capability == SlotlessItemHandlerConfig.CAPABILITY)
                return SlotlessItemHandlerConfig.CAPABILITY.cast(itemHandler);
            return null;
        }
    }
}
