package com.mike_caron.equivalentintegrations.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.icu.text.UTF16;
import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.storage.EMCInventory;
import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.Comparator;

public class DumpCacheCommand
        extends CommandBase
{
    @Override
    public String getName()
    {
        return "dumpeicache";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "dumpeicache";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(sender instanceof EntityPlayer)
        {
            //EMCItemHandler.cleanupKnowledge((EntityPlayer)sender);
            EMCInventory inventory = EquivalentIntegrationsMod.emcManager.getEMCInventory(((EntityPlayer) sender).getUniqueID());

            if(inventory != null)
            {
                Gson gson = new Gson();
                EquivalentIntegrationsMod.logger.info("Starting dump of cached inventory:");
                StringBuilder dump = new StringBuilder();
                inventory
                    .getCachedInventory()
                    .stream()
                    .sorted(Comparator.comparing(ItemStack::getDisplayName))
                    .forEachOrdered(i -> dump
                        .append(i.getDisplayName())
                        .append(" x ")
                        .append(i.getCount())
                        .append("\r\n"));

                EquivalentIntegrationsMod.logger.info(dump.toString());
                EquivalentIntegrationsMod.logger.info("Dump finished");
            }
        }
    }
}
