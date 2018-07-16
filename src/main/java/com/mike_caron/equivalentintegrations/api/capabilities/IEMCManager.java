package com.mike_caron.equivalentintegrations.api.capabilities;

import java.util.UUID;

public interface IEMCManager
{
    double getEMC(UUID player);
    void setEMC(UUID player, double newEmc);

    long withdrawEMC(UUID player, long amt);
    void depositEMC(UUID player, long amt);

    void tick();

    void playerLoggedIn(UUID owner);
}
