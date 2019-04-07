package com.mike_caron.equivalentintegrations.integrations.projecte;

import com.latmod.mods.projectex.integration.PersonalEMC;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ProjectEXWrapper extends ProjectEWrapper
{
    @Override
    public boolean isSafe()
    {
        return true;
    }

    @Override
    protected IKnowledgeProvider getKnowledgeProvider(@Nonnull World world, @Nonnull UUID owner)
    {
        return PersonalEMC.get(world, owner);
    }

    @Override
    protected IKnowledgeProvider getKnowledgeProvider(@Nonnull EntityPlayer player)
    {
        return PersonalEMC.get(player);
    }

    public void test()
        throws NoClassDefFoundError
    {
        PersonalEMC.class.getName();
    }
}
