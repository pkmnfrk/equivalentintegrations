package com.mike_caron.equivalentintegrations.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

public class EMCChangedEvent extends Event
{
    public final UUID player;
    public final double newEmc;

    public EMCChangedEvent(UUID player, double newEmc)
    {
        this.player = player;
        this.newEmc = newEmc;
    }

    @Override
    public boolean isCancelable()
    {
        return false;
    }
}
