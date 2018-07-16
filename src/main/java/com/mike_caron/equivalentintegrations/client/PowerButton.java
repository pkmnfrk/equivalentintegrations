package com.mike_caron.equivalentintegrations.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class PowerButton extends GuiButton
{
    public PowerButton(int buttonID, int posX, int posY)
    {
        super(buttonID, posX, posY, 16, 16, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
    }
}

