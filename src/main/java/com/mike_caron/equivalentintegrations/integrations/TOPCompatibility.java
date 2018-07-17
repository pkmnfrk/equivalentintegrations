package com.mike_caron.equivalentintegrations.integrations;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TOPCompatibility
{
    private static boolean registered = false;

    public static void register()
    {
        if(registered) return;

        registered = true;
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "com.mike_caron.equivalentintegrations.integrations.TOPCompatibility$GetTheOneProbe");
    }

    public static class GetTheOneProbe
            implements Function<ITheOneProbe, Void>
    {
        public static ITheOneProbe probe;

        @Override
        @Nullable
        public Void apply(ITheOneProbe iTheOneProbe)
        {
            probe = iTheOneProbe;
            EquivalentIntegrationsMod.logger.info("Enabled support for The One Probe. Most excellent.");
            probe.registerProvider(new IProbeInfoProvider()
            {
                @Override
                public String getID()
                {
                    return EquivalentIntegrationsMod.modId + ":default";
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
                {
                    if(blockState.getBlock() instanceof ITOPInfoProvider)
                    {
                        ITOPInfoProvider provider = (ITOPInfoProvider) blockState.getBlock();
                        provider.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                    }
                }
            });
            return null;
        }
    }
}
