package com.mike_caron.equivalentintegrations.integrations.refinedstorage;

import com.raoulvdberge.refinedstorage.api.IRSAPI;
import com.raoulvdberge.refinedstorage.api.RSAPIInject;

public class RSAPIHelper
{
    @RSAPIInject
    public static IRSAPI rsAPI;
}
