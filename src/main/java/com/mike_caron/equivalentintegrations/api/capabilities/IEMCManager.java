package com.mike_caron.equivalentintegrations.api.capabilities;

import java.util.UUID;

public interface IEMCManager
{
    double getEMC(UUID player);
    void setEMC(UUID player, double newEmc);
}
