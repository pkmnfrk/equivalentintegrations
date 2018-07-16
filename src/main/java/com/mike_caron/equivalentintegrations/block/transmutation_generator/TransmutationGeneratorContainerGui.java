package com.mike_caron.equivalentintegrations.block.transmutation_generator;


import com.mike_caron.equivalentintegrations.EquivalentIntegrationsMod;
import com.mike_caron.equivalentintegrations.network.CtoSMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class TransmutationGeneratorContainerGui extends GuiContainer
{
    public static final int WIDTH = 182;
    public static final int HEIGHT = 154;

    public static final int INCREASE_ID = 1;
    public static final int DECREASE_ID = INCREASE_ID + 1;
    public static final int POWER_ID = DECREASE_ID + 1;

    public static final int FONT_COLOUR = 0x404040;

    private boolean shiftHeld, altHeld, ctrlHeld;
    private boolean lastShiftHeld, lastAltHeld, lastCtrlHeld;

    private final TransmutationGeneratorTileEntity tileEntity;
    private final TransmutationGeneratorContainer container;

    private GuiButton decreaseButton;
    private GuiButton increaseButton;
    private PowerButton powerButton;

    private static final ResourceLocation background = new ResourceLocation(EquivalentIntegrationsMod.modId, "textures/gui/transmutation_generator_gui.png");

    public TransmutationGeneratorContainerGui(TransmutationGeneratorTileEntity tileEntity, TransmutationGeneratorContainer container)
    {
        super(container);

        xSize = WIDTH;
        ySize = HEIGHT;

        this.tileEntity = tileEntity;
        this.container = container;

    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.buttonList.clear();

        int amt = getDeltaAmt();

        this.buttonList.add(decreaseButton = new GuiButton( DECREASE_ID,guiLeft + 83,  guiTop + 25, 30, 20, "-" + amt));
        this.buttonList.add(powerButton = new PowerButton( POWER_ID,guiLeft + 118, guiTop + 25));
        this.buttonList.add(increaseButton = new GuiButton( INCREASE_ID,guiLeft + 143, guiTop + 25, 30, 20, "+" + amt));
        updateGui(true);
    }

    private void updateGui(boolean labels)
    {
        int amt = getDeltaAmt();

        if(labels)
        {
            decreaseButton.displayString = "-" + amt;
            increaseButton.displayString = "+" + amt;
        }

        decreaseButton.enabled = !(tileEntity.getPowerPerTick() - amt <= 0);
        increaseButton.enabled = !(tileEntity.getPowerPerTick() + amt > 50000);

        //powerButton.displayString = tileEntity.getGenerating() ? "O" : "X";
        powerButton.setState(tileEntity.getGenerating());
    }

    private int getDeltaAmt()
    {
        int amt = 10;

        if(shiftHeld) amt *= 10;
        if(ctrlHeld) amt *= 5;
        if(altHeld) amt /= 2;

        return amt;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void handleKeyboardInput() throws IOException
    {
        super.handleKeyboardInput();
        lastShiftHeld = shiftHeld;
        lastAltHeld = altHeld;
        lastCtrlHeld = ctrlHeld;

        shiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        altHeld = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
        ctrlHeld = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

        if(shiftHeld != lastShiftHeld || altHeld != lastAltHeld || ctrlHeld != lastCtrlHeld) {
            updateGui(true);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(new TextComponentTranslation("container.transmutation_generator.title", new Object[0]).getUnformattedText(), 8, 6, FONT_COLOUR);

        String powerPerTick = String.format("%d", tileEntity.getPowerPerTick());
        int w = this.fontRenderer.getStringWidth(powerPerTick);
        int x = 128 - w / 2;
        this.fontRenderer.drawString(powerPerTick, x, 53, FONT_COLOUR);

    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        updateGui(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(!button.enabled) return;

        switch(button.id)
        {
            case INCREASE_ID:
                {
                    int amt = getDeltaAmt();
                    if(amt + tileEntity.getPowerPerTick() < 50000)
                    {
                        IMessage msg = new CtoSMessage(tileEntity.getWorld().provider.getDimension(), tileEntity.getPos(), amt);
                        EquivalentIntegrationsMod.networkWrapper.sendToServer(msg);
                    }
                }
                break;
            case DECREASE_ID:
                {
                    int amt = -getDeltaAmt();
                    if(amt + tileEntity.getPowerPerTick() > 0)
                    {
                        IMessage msg = new CtoSMessage(tileEntity.getWorld().provider.getDimension(), tileEntity.getPos(), amt);
                        EquivalentIntegrationsMod.networkWrapper.sendToServer(msg);
                    }
                }
                break;
            case POWER_ID:
                {
                    boolean onOff = !tileEntity.getGenerating();
                    IMessage msg = new CtoSMessage(tileEntity.getWorld().provider.getDimension(), tileEntity.getPos(), onOff);
                    EquivalentIntegrationsMod.networkWrapper.sendToServer(msg);
                }
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @SideOnly(Side.CLIENT)
    public class PowerButton extends GuiButton
    {
        private boolean state = false;

        public PowerButton(int buttonID, int posX, int posY)
        {
            super(buttonID, posX, posY, 20, 20, "");
        }

        public boolean getState() { return state;}
        public void setState(boolean newState) { state = newState;}

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            super.drawButton(mc, mouseX, mouseY, partialTicks);
            mc.getTextureManager().bindTexture(background);
            GL11.glColor4f(1f,1f,1f,1f);
            int ty = state ? 0 : 16;
            this.drawTexturedModalRect(this.x + 2, this.y + 2, 192, ty, 16, 16);

        }
    }
}
