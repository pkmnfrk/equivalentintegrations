package com.mike_caron.equivalentintegrations.command;

import com.mike_caron.equivalentintegrations.storage.EMCItemHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CleanupCommand
        extends CommandBase
{
    @Override
    public String getName()
    {
        return "cleanknowledge";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "cleanknowledge";
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
            EMCItemHandler.cleanupKnowledge((EntityPlayer)sender);
        }
    }
}
