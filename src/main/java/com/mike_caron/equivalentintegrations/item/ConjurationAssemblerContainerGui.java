package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberContainer;
import com.mike_caron.equivalentintegrations.block.transmutation_chamber.TransmutationChamberTileEntity;
import com.mike_caron.equivalentintegrations.network.CtoSMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

public class ConjurationAssemblerContainerGui extends GuiContainer
{
    public static final int WIDTH = 182;
    public static final int HEIGHT = 154;

    private static final ResourceLocation background = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/conjuration_assembler_gui.png");

    private GuiButtonToggle[] colors;
    private final int[] colorRemap = {
        0x0, 0x1, 0x2, 0x3,
        0x8, 0x9, 0xa, 0xb,
        0x4, 0x5, 0x6, 0x7,
        0xc, 0xd, 0xe, 0xf
    };

    private ConjurationAssemblerContainer container;

    private int currentColor = 9;

    public ConjurationAssemblerContainerGui(ConjurationAssemblerContainer container) {
        super(container);

        this.container = container;

        currentColor = container.getCurrentColor();

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        colors = new GuiButtonToggle[16];
        for(int i = 0; i < 16; i++)
        {
            colors[i] = new GuiButtonToggle(i, guiLeft + (colorRemap[i] % 4) * 12 + 9, guiTop + (colorRemap[i] / 4) * 12 + 20, 12, 12, false);
            colors[i].initTextureValues((i % 4) * 24, (i / 4) * 24 + 156, 12, 12, background);
            this.addButton(colors[i]);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        currentColor = button.id;
        container.setCurrentColor(currentColor);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if(!colors[currentColor].isStateTriggered())
        {
            for(int i = 0; i < colors.length; i++)
            {
                colors[i].setStateTriggered(i == currentColor);
            }
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
        this.fontRenderer.drawString(new TextComponentTranslation("container.conjuration_assembler.title", new Object[0]).getUnformattedText(), 8, 6, 4210752);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
