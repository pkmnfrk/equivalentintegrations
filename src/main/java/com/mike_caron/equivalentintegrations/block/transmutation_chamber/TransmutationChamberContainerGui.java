package com.mike_caron.equivalentintegrations.block.transmutation_chamber;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.item.ModItems;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.xml.soap.Text;
import java.util.List;

public class TransmutationChamberContainerGui extends GuiContainer
{
    public static final int WIDTH = 182;
    public static final int HEIGHT = 154;

    private static final ResourceLocation background = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/transmutation_chamber_gui.png");

    TransmutationChamberTileEntity te;

    public TransmutationChamberContainerGui(TransmutationChamberTileEntity tileEntity, TransmutationChamberContainer container) {
        super(container);

        xSize = WIDTH;
        ySize = HEIGHT;

        this.te = tileEntity;
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
        this.fontRenderer.drawString(new TextComponentTranslation("container.transmutation_chamber.title", new Object[0]).getUnformattedText(), 8, 6, 4210752);

        if(te.getForbidNbt())
        {
            drawTexturedModalRect(148, 21, 201, 0, 18, 18);
        }

        if(te.getForbidDamage())
        {
            drawTexturedModalRect(148, 41, 201, 19, 18, 18);
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

        if(stack.getItem() == ModItems.efficiencyCatalyst)
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
