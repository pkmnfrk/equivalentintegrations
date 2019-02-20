package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.item.ModItems;
import com.mike_caron.equivalentintegrations.network.CtoSMessage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.List;

public class TransmutationChamberContainerGui extends GuiContainer
{
    public static final int WIDTH = 182;
    public static final int HEIGHT = 154;

    private static final ResourceLocation background_chamber = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/transmutation_chamber_gui.png");
    private static final ResourceLocation background_disassembler = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/transmutation_disassembler_gui.png");

    private final ResourceLocation background;

    TransmutationChamberTileEntity te;

    public TransmutationChamberContainerGui(TransmutationChamberTileEntity tileEntity, TransmutationChamberContainer container)
    {
        super(container);

        xSize = WIDTH;
        ySize = HEIGHT;

        this.te = tileEntity;

        if (te.getType() == 0)
        {
            background = background_chamber;
        }
        else if (te.getType() == 1)
        {
            background = background_disassembler;
        }
        else
        {
            throw new Error("Unknown type!?");
        }

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String title = "container.transmutation_chamber.title";
        if(te.getType() == 1)
        {
            title = "container.transmutation_disassembler.title";
        }

        this.fontRenderer.drawString(new TextComponentTranslation(title, new Object[0]).getUnformattedText(), 8, 6, 4210752);
        if(te.getForbidDamage() || te.getForbidNbt())
        {
            mc.getTextureManager().bindTexture(background);
            GlStateManager.color(1, 1, 1, 1);
            if (te.getForbidNbt())
            {
                drawTexturedModalRect(148, 21, 182, 0, 18, 18);
            }

            if (te.getForbidDamage())
            {
                drawTexturedModalRect(148, 41, 182, 20, 18, 18);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if(mouseX - guiLeft >= 148 && mouseX - guiLeft < 166)
        {
            if(mouseY - guiTop >= 21 && mouseY - guiTop < 39)
            {
                boolean n = !te.getForbidNbt();
                CtoSMessage msg = new CtoSMessage(0, te.getPos(), CtoSMessage.KindEnum.ToggleForbidNbt, n);
                EquivalentIntegrationsMod.networkWrapper.sendToServer(msg);
            }
            else if(mouseY - guiTop >= 41 && mouseY - guiTop < 59)
            {
                boolean n = !te.getForbidDamage();
                CtoSMessage msg = new CtoSMessage(0, te.getPos(), CtoSMessage.KindEnum.ToggleForbidDamage, n);
                EquivalentIntegrationsMod.networkWrapper.sendToServer(msg);
            }
        }
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY)
    {
        super.renderHoveredToolTip(mouseX, mouseY);

        if(mouseX - guiLeft >= 148 && mouseX - guiLeft < 166)
        {
            if(mouseY - guiTop >= 21 && mouseY - guiTop < 39)
            {
                this.drawHoveringText(new TextComponentTranslation("container.transmutation_chamber.forbidnbt").getFormattedText(), mouseX, mouseY);
            }
            else if(mouseY - guiTop >= 41 && mouseY - guiTop < 59)
            {
                this.drawHoveringText(new TextComponentTranslation("container.transmutation_chamber.forbiddamaged").getFormattedText(), mouseX, mouseY);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public List<String> getItemToolTip(ItemStack stack)
    {
        List<String> ret = super.getItemToolTip(stack);

        if(te.getType() == 0 && stack.getItem() == ModItems.efficiencyCatalyst)
        {
            StringBuilder sb = new StringBuilder();

            sb.append(TextFormatting.GOLD);
            sb.append(new TextComponentTranslation("container.transmutation_chamber.efficiency").getFormattedText());
            sb.append(TextFormatting.RESET);
            sb.append(" ");

            if(stack.getCount() < 4)
            {
                String eff;

                eff = Integer.toString(TransmutationChamberTileEntity.getEfficiencyThreshold(stack.getCount()));

                sb.append(eff);
            }
            else
            {
                sb.append(new TextComponentTranslation("container.transmutation_chamber.efficiency_inf").getFormattedText());
            }

            int pos = 2;
            ret.add(pos, sb.toString());
        }

        return ret;
    }
}
