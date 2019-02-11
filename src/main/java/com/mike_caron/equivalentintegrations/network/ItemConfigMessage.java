package com.mike_caron.equivalentintegrations.network;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.equivalentintegrations.block.transmutation_generator.TransmutationGeneratorTileEntity;
import com.mike_caron.equivalentintegrations.item.IItemConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ItemConfigMessage
    implements IMessage
{

    private int discriminator;
    private int payload;

    public ItemConfigMessage() {}
    public ItemConfigMessage(int discriminator, int payload)
    {
        this.discriminator = discriminator;
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        discriminator = buf.readInt();
        payload = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(discriminator);
        buf.writeInt(payload);
    }

    public int getDiscriminator()
    {
        return discriminator;
    }

    public int getPayload()
    {
        return payload;
    }

    public static class Handler
        implements IMessageHandler<ItemConfigMessage, IMessage>
    {
        @Override
        public IMessage onMessage(ItemConfigMessage message, MessageContext ctx)
        {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            final World world = player.world;

            final IThreadListener mainThread = (WorldServer)world;
            mainThread.addScheduledTask(() -> {
                try {

                    //look for the player's current stack
                    ItemStack stack = player.inventory.getCurrentItem();

                    //look for the Item of that stack
                    Item item = stack.getItem();


                    if(item instanceof IItemConfig)
                    {
                        ((IItemConfig) item).onConfig(stack, message.discriminator, message.payload);
                    }

                }
                catch (Exception e)
                {
                    EquivalentIntegrationsMod.logger.error("Error while handling message", e);
                }
            });

            return null;
        }
    }
}
