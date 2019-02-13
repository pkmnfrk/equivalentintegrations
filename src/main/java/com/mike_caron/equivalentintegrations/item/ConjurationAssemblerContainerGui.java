package com.mike_caron.equivalentintegrations.item;

import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.mikesmodslib.gui.GuiButton;
import com.mike_caron.mikesmodslib.gui.GuiContainerBase;
import com.mike_caron.mikesmodslib.gui.GuiUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;

public class ConjurationAssemblerContainerGui
    extends GuiContainerBase
    implements GuiButton.ClickedListener
{
    public static final int WIDTH = 182;
    public static final int HEIGHT = 154;

    private static final ResourceLocation background = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/conjuration_assembler_gui.png");

    private ColorButton[] colors;
    private final int[] colorRemap = {
        0x0, 0x1, 0x2, 0x3,
        0x8, 0x9, 0xa, 0xb,
        0x4, 0x5, 0x6, 0x7,
        0xc, 0xd, 0xe, 0xf
    };

    private ConjurationAssemblerContainer container;

    public ConjurationAssemblerContainerGui(ConjurationAssemblerContainer container) {
        super(container, WIDTH, HEIGHT);

        this.container = container;

        initControls();
    }

    @Override
    protected void addControls()
    {
        super.addControls();

        colors = new ColorButton[16];
        for(int i = 0; i < 16; i++)
        {
            colors[i] = new ColorButton(i, (colorRemap[i] % 4) * 12 + 9, (colorRemap[i] / 4) * 12 + 20);
            if(container.currentColor == i)
                colors[i].setStateTriggered(true);
            this.addControl(colors[i]);
            colors[i].addListener(this);
        }
    }

    @Override
    public void clicked(GuiButton.ClickedEvent clickedEvent)
    {
        container.setCurrentColor(clickedEvent.id);
    }

    @Override
    public void onContainerRefresh()
    {
        if(colors != null)
        {
            for(int i = 0; i < colors.length; i++)
            {
                colors[i].setStateTriggered(i == container.currentColor);
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
    protected String getTitleKey()
    {
        return "container.conjuration_assembler.title";
    }

    private class ColorButton
        extends GuiButton
    {
        private final int sx, sy;
        private boolean toggled = false;

        public ColorButton(int id, int x, int y, int width, int height)
        {
            super(id, x, y, width, height, null);
            sx = (id % 4) * width * 2;
            sy = (id / 4) * (height * 2) + 156;
        }

        public boolean isStateTriggered()
        {
            return toggled;
        }

        public void setStateTriggered(boolean state)
        {
            this.toggled = state;
        }

        public ColorButton(int id, int x, int y)
        {
            this(id, x, y, 12, 12);
        }

        @Override
        public void draw()
        {
            GuiButton.State state = this.state;

            if(toggled) state = State.PRESSED;

            int tx = sx;
            int ty = sy;

            if(state == State.PRESSED)
                tx += width;
            if(state == State.HOVERED)
                ty += height;

            GuiUtil.bindTexture(background);
            GuiUtil.setGLColor(Color.WHITE);
            GuiUtil.drawTexturePart(0, 0, width, height, tx, ty, 256, 256);
        }
    }
}
