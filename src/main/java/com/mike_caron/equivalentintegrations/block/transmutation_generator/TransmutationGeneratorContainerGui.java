package com.mike_caron.equivalentintegrations.block.transmutation_generator;


import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class TransmutationGeneratorContainerGui extends GuiContainer
{
    public static final int WIDTH = 182;
    public static final int HEIGHT = 154;

    private static final ResourceLocation background = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/transmutation_generator_gui.png");

    public TransmutationGeneratorContainerGui(TransmutationGeneratorTileEntity tileEntity, TransmutationGeneratorContainer container)
    {
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

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(new TextComponentTranslation("container.transmutation_generator.title", new Object[0]).getUnformattedText(), 8, 6, 4210752);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
