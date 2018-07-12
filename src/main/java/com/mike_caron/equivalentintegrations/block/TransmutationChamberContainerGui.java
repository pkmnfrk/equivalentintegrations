package com.mike_caron.equivalentintegrations.block;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class TransmutationChamberContainerGui extends GuiContainer
{
    public static final int WIDTH = 180;
    public static final int HEIGHT = 152;

    private static final ResourceLocation background = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/transmutation_chamber_gui.png");

    public TransmutationChamberContainerGui(TransmutationChamberTileEntity tileEntity, TransmutationChamberContainer container) {
        super(container);

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
