package com.mike_caron.equivalentintegrations.integrations.refinedstorage;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.raoulvdberge.refinedstorage.api.storage.StorageType;

public class RefinedStorageCompatibility
{
    private static boolean registered = false;

    public static void register()
    {
        if(registered) return;

        registered = true;

        RSAPIHelper.rsAPI.addExternalStorageProvider(StorageType.ITEM, new TransmutationExternalStorageProvider());

        EquivalentIntegrationsMod.logger.info("Refined Storage integration, locked and loaded");
    }
}
