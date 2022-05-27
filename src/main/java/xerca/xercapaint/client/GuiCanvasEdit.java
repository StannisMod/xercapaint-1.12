package xerca.xercapaint.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.PaletteUtil;
import xerca.xercapaint.common.SoundEvents;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityEasel;
import xerca.xercapaint.common.packets.CanvasMiniUpdatePacket;
import xerca.xercapaint.common.packets.CanvasUpdatePacket;
import xerca.xercapaint.common.packets.EaselLeftPacket;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiCanvasEdit extends BasePalette {
    private double canvasX;
    private double canvasY;
    private static final double[] canvasXs = {-1000, -1000, -1000, -1000};
    private static final double[] canvasYs = {-1000, -1000, -1000, -1000};
    private final int canvasWidth;
    private final int canvasHeight;
    private int brushMeterX;
    private int brushMeterY;
    private int brushOpacityMeterX;
    private int brushOpacityMeterY;
    private final int canvasPixelScale;
    private final int canvasPixelWidth;
    private final int canvasPixelHeight;
    private int brushSize = 0;
    private boolean touchedCanvas = false;
    private boolean undoStarted = false;
    private boolean gettingSigned;
    private boolean isCarryingCanvas;
    private GuiButton buttonSign;
    private GuiButton buttonCancel;
    private GuiButton buttonFinalize;
    private GuiButton buttonHelpToggle;
    private int updateCount;
    private BrushSound brushSound = null;
    private final int canvasHolderHeight = 10;
    private static int brushOpacitySetting = 0;
    private static final float[] brushOpacities = {1.f, 0.75f, 0.5f, 0.25f};
    private static boolean showHelp = false;
    private final Set<Integer> draggedPoints = new HashSet<>();

    private final EntityPlayer editingPlayer;

    private CanvasType canvasType;
    private boolean isSigned = false;
    private int[] pixels;
    private String authorName = "";
    private String canvasTitle = "";
    private String name = "";
    private int version = 0;
    private EntityEasel easel;
    private int timeSinceLastUpdate = 0;
    private boolean skippedUpdate = false;

    private int lastMouseX;
    private int lastMouseY;

    private static final Vec2f[] outlinePoss1 = {
            new Vec2f(0.f, 199.0f),
            new Vec2f(12.f, 199.0f),
            new Vec2f(34.f, 199.0f),
            new Vec2f(76.f, 199.0f),
    };

    private static final Vec2f[] outlinePoss2 = {
            new Vec2f(128.f, 199.0f),
            new Vec2f(135.f, 199.0f),
            new Vec2f(147.f, 199.0f),
            new Vec2f(169.f, 199.0f),
    };

    private static final int maxUndoLength = 16;
    private Deque<int[]> undoStack = new ArrayDeque<>(maxUndoLength);

    protected GuiCanvasEdit(EntityPlayer player, NBTTagCompound canvasTag, NBTTagCompound paletteTag, ITextComponent title, CanvasType canvasType) {
        this(player, canvasTag, paletteTag, title, canvasType, null);
    }

    protected GuiCanvasEdit(EntityPlayer player, NBTTagCompound canvasTag, NBTTagCompound paletteTag, ITextComponent title, CanvasType canvasType, EntityEasel easel) {
        super(title, paletteTag);

        paletteX = 40;
        paletteY = 40;
        this.canvasType = canvasType;
        this.canvasPixelScale = canvasType == CanvasType.SMALL ? 10 : 5;
        this.canvasPixelWidth = CanvasType.getWidth(canvasType);
        this.canvasPixelHeight = CanvasType.getHeight(canvasType);
        int canvasPixelArea = canvasPixelHeight * canvasPixelWidth;
        this.canvasWidth = this.canvasPixelWidth * this.canvasPixelScale;
        this.canvasHeight = this.canvasPixelHeight * this.canvasPixelScale;
        this.easel = easel;

        if (canvasType.equals(CanvasType.LONG)) {
            this.canvasY += 40;
        }

        this.editingPlayer = player;
        if (canvasTag != null && !canvasTag.hasNoTags()) {
            int[] nbtPixels = canvasTag.getIntArray("pixels");
            this.authorName = canvasTag.getString("author");
            this.canvasTitle = canvasTag.getString("title");
            this.name = canvasTag.getString("name");
            this.version = canvasTag.getInteger("v");

            this.pixels = Arrays.copyOfRange(nbtPixels, 0, canvasPixelArea);
        } else {
            this.isSigned = false;
        }

        if (this.pixels == null) {
            this.pixels = new int[canvasPixelArea];
            Arrays.fill(this.pixels, basicColors[15].rgbVal());

            long secs = System.currentTimeMillis() / 1000;
            this.name = "" + player.getUniqueID() + "_" + secs;
        }

        if (paletteComplete) {
            XercaPaint.LOGGER.warn("Is complete");
        }
    }

    private void playBrushSound() {
        brushSound = new BrushSound();
        playSound(brushSound);
    }

    private int getPixelAt(int x, int y) {
        return this.pixels[y * canvasPixelWidth + x];
    }

    private void setPixelAt(int x, int y, PaletteUtil.Color color, float opacity) {
        if (x >= 0 && y >= 0 && x < canvasPixelWidth && y < canvasPixelHeight) {
            if (!draggedPoints.contains(y * canvasPixelWidth + x)) {
                draggedPoints.add(y * canvasPixelWidth + x);
                this.pixels[y * canvasPixelWidth + x] = PaletteUtil.Color.mix(color, new PaletteUtil.Color(this.pixels[y * canvasPixelWidth + x]), opacity).rgbVal();
            }
        }
    }

    private void setPixelsAt(int mouseX, int mouseY, PaletteUtil.Color color, int brushSize, float opacity) {
        int x, y;
        final int pixelHalf = canvasPixelScale / 2;
        switch (brushSize) {
            case 0:
                x = (mouseX - (int) canvasX) / canvasPixelScale;
                y = (mouseY - (int) canvasY) / canvasPixelScale;
                setPixelAt(x, y, color, opacity);
                break;
            case 1:
                x = (mouseX - (int) canvasX + pixelHalf) / canvasPixelScale;
                y = (mouseY - (int) canvasY + pixelHalf) / canvasPixelScale;
                setPixelAt(x, y, color, opacity);
                setPixelAt(x - 1, y, color, opacity);
                setPixelAt(x, y - 1, color, opacity);
                setPixelAt(x - 1, y - 1, color, opacity);
                break;
            case 2:
                x = (mouseX - (int) canvasX + pixelHalf) / canvasPixelScale;
                y = (mouseY - (int) canvasY + pixelHalf) / canvasPixelScale;
                setPixelAt(x - 1, y + 1, color, opacity);
                setPixelAt(x, y + 1, color, opacity);
                setPixelAt(x - 2, y, color, opacity);
                setPixelAt(x - 1, y, color, opacity);
                setPixelAt(x, y, color, opacity);
                setPixelAt(x + 1, y, color, opacity);
                setPixelAt(x - 2, y - 1, color, opacity);
                setPixelAt(x - 1, y - 1, color, opacity);
                setPixelAt(x, y - 1, color, opacity);
                setPixelAt(x + 1, y - 1, color, opacity);
                setPixelAt(x - 1, y - 2, color, opacity);
                setPixelAt(x, y - 2, color, opacity);
                break;
            case 3:
                x = (mouseX - (int) canvasX) / canvasPixelScale;
                y = (mouseY - (int) canvasY) / canvasPixelScale;
                setPixelAt(x - 1, y + 2, color, opacity);
                setPixelAt(x + 0, y + 2, color, opacity);
                setPixelAt(x + 1, y + 2, color, opacity);
                setPixelAt(x - 2, y + 1, color, opacity);
                setPixelAt(x - 1, y + 1, color, opacity);
                setPixelAt(x + 0, y + 1, color, opacity);
                setPixelAt(x + 1, y + 1, color, opacity);
                setPixelAt(x + 2, y + 1, color, opacity);
                setPixelAt(x - 2, y, color, opacity);
                setPixelAt(x - 1, y, color, opacity);
                setPixelAt(x + 0, y, color, opacity);
                setPixelAt(x + 1, y, color, opacity);
                setPixelAt(x + 2, y, color, opacity);
                setPixelAt(x - 2, y - 1, color, opacity);
                setPixelAt(x - 1, y - 1, color, opacity);
                setPixelAt(x + 0, y - 1, color, opacity);
                setPixelAt(x + 1, y - 1, color, opacity);
                setPixelAt(x + 2, y - 1, color, opacity);
                setPixelAt(x - 1, y - 2, color, opacity);
                setPixelAt(x + 0, y - 2, color, opacity);
                setPixelAt(x + 1, y - 2, color, opacity);
                break;
        }
    }

    private void resetPositions() {
        final int padding = 40;
        final int paletteCanvasX = (this.width - (paletteWidth + canvasWidth + padding)) / 2;
        canvasX = paletteCanvasX + paletteWidth + padding;
        if (canvasType.equals(CanvasType.LONG)) {
            canvasY = 80;
        } else {
            canvasY = 40;
        }

        paletteX = paletteCanvasX;
        paletteY = 40;
    }

    @Override
    public void initGui() {
        canvasX = canvasXs[canvasType.ordinal()];
        canvasY = canvasYs[canvasType.ordinal()];
        paletteX = paletteXs[canvasType.ordinal()];
        paletteY = paletteYs[canvasType.ordinal()];
        if (canvasX == -1000 || canvasY == -1000 || paletteX == -1000 || paletteY == -1000) {
            resetPositions();
        }

        updateCanvasPos(0, 0);
        updatePalettePos(0, 0);

        ScaledResolution scaled = new ScaledResolution(mc);

        int x = scaled.getScaledWidth() - 120;
        int y = scaled.getScaledHeight() - 30;
        this.buttonSign = this.addButton(new GuiButton(0, x, y, 98, 20, I18n.format("canvas.signButton")));
        this.buttonFinalize = this.addButton(new GuiButton(1, (int) canvasX - 100, 100, 98, 20, I18n.format("canvas.finalizeButton")));
        this.buttonCancel = this.addButton(new GuiButton(2, (int) canvasX - 100, 130, 98, 20, I18n.format("gui.cancel")));

        x = (int) (scaled.getScaledWidth() * 0.95) - 21;
        y = (int) (scaled.getScaledHeight() * 0.05);
        // TODO
//        this.buttonHelpToggle = this.addButton(new GuiButtonToggle(3, x, y, 21, 21, 197, 0, 21,
//                paletteTextures, 256, 256, button -> {
//            showHelp = !showHelp;
//        }, (button, poseStack, i, j) -> renderTooltip(poseStack, new TextComponentString("Toggle help tooltips"), i, j)));

        updateButtons();

        Mouse.setGrabbed(true);
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 0:
                if (!isSigned) {
                    gettingSigned = true;
                    resetPositions();
                    updateButtons();

                    Mouse.setGrabbed(false);
                }
                break;
            case 1:
                if (!isSigned) {
                    dirty = true;
                    isSigned = true;
                    if (mc != null) {
                        mc.displayGuiScreen(null);
                    }
                }
                break;
            case 2:
                if (!isSigned) {
                    gettingSigned = false;
                    updateButtons();

                    Mouse.setGrabbed(true);
                }
                break;
        }
    }

    private void updateButtons() {
        if (!this.isSigned) {
            this.buttonSign.visible = !this.gettingSigned;
            this.buttonCancel.visible = this.gettingSigned;
            this.buttonFinalize.visible = this.gettingSigned;
            this.buttonFinalize.enabled = !this.canvasTitle.trim().isEmpty();

            this.buttonFinalize.x = (int) canvasX - 100;
            this.buttonCancel.x = (int) canvasX - 100;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        // Draw the canvas
        for (int i = 0; i < canvasPixelHeight; i++) {
            for (int j = 0; j < canvasPixelWidth; j++) {
                int y = (int) (canvasY + i * canvasPixelScale);
                int x = (int) (canvasX + j * canvasPixelScale);
                drawRect(x, y, x + canvasPixelScale, y + canvasPixelScale, getPixelAt(j, i));
            }
        }

        // Draw brush meter
        for (int i = 0; i < 4; i++) {
            int y = brushMeterY + i * brushSpriteSize;
            drawRect(brushMeterX, y, brushMeterX + 3, y + 3, currentColor.rgbVal());
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(brushMeterX, brushMeterY + (3 - brushSize) * brushSpriteSize, 15, 246, 10, 10);
        drawTexturedModalRect(brushMeterX, brushMeterY, brushSpriteX, brushSpriteY - brushSpriteSize * 3, brushSpriteSize, brushSpriteSize * 4);

        // Draw brush and outline
        renderCursor(mouseX, mouseY);
    }


    public static void setGLColor(PaletteUtil.Color c) {
        GlStateManager.color(((float) c.r) / 255.f, ((float) c.g) / 255.f, ((float) c.b) / 255.f, 1.0f);
    }

    private void renderCursor(int mouseX, int mouseY) {
        if (isCarryingColor) {
            setGLColor(carriedColor);
            drawTexturedModalRect(mouseX - brushSpriteSize / 2, mouseY - brushSpriteSize / 2, brushSpriteX + brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);

        } else if (isCarryingWater) {
            setGLColor(waterColor);
            drawTexturedModalRect(mouseX - brushSpriteSize / 2, mouseY - brushSpriteSize / 2, brushSpriteX + brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);
        } else if (isPickingColor) {
            drawOutline(mouseX, mouseY, 0);
            setGLColor(PaletteUtil.Color.WHITE);
            drawTexturedModalRect(mouseX, mouseY - colorPickerSize, colorPickerSpriteX, colorPickerSpriteY, colorPickerSize, colorPickerSize);
        } else {
            drawOutline(mouseX, mouseY, brushSize);
            drawRect(mouseX, mouseY, mouseX + 3, mouseY + 3, currentColor.rgbVal());

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int trueBrushY = brushSpriteY - brushSpriteSize * brushSize;
            drawTexturedModalRect(mouseX, mouseY, brushSpriteX, trueBrushY, brushSpriteSize, brushSpriteSize);
        }
    }

    private void drawOutline(int mouseX, int mouseY, int brushSize) {
        if (inCanvas(mouseX, mouseY)) {
            // Render drawing outline
            int x = 0;
            int y = 0;
            int outlineSize = 0;
            int pixelHalf = canvasPixelScale / 2;
            if (brushSize == 0) {
                x = ((mouseX - (int) canvasX) / canvasPixelScale) * canvasPixelScale + (int) canvasX - 1;
                y = ((mouseY - (int) canvasY) / canvasPixelScale) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale + 2;
            }
            if (brushSize == 1) {
                x = (((mouseX - (int) canvasX + pixelHalf) / canvasPixelScale) - 1) * canvasPixelScale + (int) canvasX - 1;
                y = (((mouseY - (int) canvasY + pixelHalf) / canvasPixelScale) - 1) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale * 2 + 2;
            }
            if (brushSize == 2) {
                x = (((mouseX - (int) canvasX + pixelHalf) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasX - 1;
                y = (((mouseY - (int) canvasY + pixelHalf) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale * 4 + 2;
            }
            if (brushSize == 3) {
                x = (((mouseX - (int) canvasX) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasX - 1;
                y = (((mouseY - (int) canvasY) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale * 5 + 2;
            }

            Vec2f textureVec;
            if (canvasPixelScale == 10) {
                textureVec = outlinePoss1[brushSize];
            } else {
                textureVec = outlinePoss2[brushSize];
            }

            GlStateManager.color(0.3F, 0.3F, 0.3F, 1.0F);
            drawTexturedModalRect(x, y, (int) textureVec.x, (int) textureVec.y, outlineSize, outlineSize);
        }
    }


    public static boolean isKeyComboCtrlZ(int keyID) {
        return keyID == Keyboard.KEY_Z && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (isKeyComboCtrlZ(keyCode)) {
            if (undoStack.size() > 0) {
                pixels = undoStack.pop();
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        int mouseX = Mouse.getEventX();
        int mouseY = Mouse.getEventY();
        if (scroll != 0) {
            if (!gettingSigned) {
                if (inBrushOpacityMeter(mouseX, mouseY)) {
                    final int maxBrushOpacity = 3;
                    brushOpacitySetting += scroll < 0 ? 1 : -1;
                    if (brushOpacitySetting > maxBrushOpacity) brushOpacitySetting = 0;
                    else if (brushOpacitySetting < 0) brushOpacitySetting = maxBrushOpacity;
                } else {
                    final int maxBrushSize = 3;
                    brushSize += scroll > 0 ? 1 : -1;
                    if (brushSize > maxBrushSize) brushSize = 0;
                    else if (brushSize < 0) brushSize = maxBrushSize;
                }
            }
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    // Mouse button 0: left, 1: right
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (gettingSigned) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        undoStarted = true;
        touchedCanvas = false;
        if (undoStack.size() >= maxUndoLength) {
            undoStack.removeLast();
        }
        undoStack.push(pixels.clone());

        if (inCanvas(mouseX, mouseY)) {
            if (isPickingColor) {
                int x = (mouseX - (int) canvasX) / canvasPixelScale;
                int y = (mouseY - (int) canvasY) / canvasPixelScale;
                if (x >= 0 && y >= 0 && x < canvasPixelWidth && y < canvasPixelHeight) {
                    int color = getPixelAt(x, y);
                    carriedColor = new PaletteUtil.Color(color);
                    setCarryingColor();
                    playSound(SoundEvents.COLOR_PICKER_SUCK);
                }
            } else {
                clickedCanvas(mouseX, mouseY, mouseButton);
                playBrushSound();
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (inBrushMeter(mouseX, mouseY)) {
            int selectedSize = 3 - (mouseY - brushMeterY) / brushSpriteSize;
            if (selectedSize <= 3 && selectedSize >= 0) {
                brushSize = selectedSize;
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        if (inBrushOpacityMeter(mouseX, mouseY)) {
            int relativeY = mouseY - brushOpacityMeterY;
            int selectedOpacity = relativeY / (brushOpacitySpriteSize + 1);
            if (selectedOpacity >= 0 && selectedOpacity <= 3) {
                brushOpacitySetting = selectedOpacity;
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        if (inCanvasHolder(mouseX, mouseY)) {
            isCarryingCanvas = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void clickedCanvas(int mouseX, int mouseY, int mouseButton) {
        touchedCanvas = true;
        if (mouseButton == 0) {
            setPixelsAt(mouseX, mouseY, currentColor, brushSize, brushOpacities[brushOpacitySetting]);
        } else if (mouseButton == 1) {
            // "Erase" with right click
            setPixelsAt(mouseX, mouseY, PaletteUtil.Color.WHITE, brushSize, 1.0f);
        }
        dirty = true;
    }

    @Override
    public void mouseReleased(int posX, int posY, int mouseButton) {
        isCarryingCanvas = false;
        if (gettingSigned) {
            super.mouseReleased(posX, posY, mouseButton);
            return;
        }
        draggedPoints.clear();

        if (undoStarted && !touchedCanvas) {
            undoStarted = false;
            undoStack.removeFirst();
        }

        if (brushSound != null) {
            brushSound.stopSound();
        }

        if (easel != null) {
            updateCanvas(false);
        }

        super.mouseReleased(posX, posY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (gettingSigned) {
            super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
            return;
        }
        if (!isCarryingColor && !isCarryingWater && !isPickingColor && !isCarryingPalette && !isCarryingCanvas) {
            if (inCanvas(mouseX, mouseY)) {
                clickedCanvas(mouseX, mouseY, mouseButton);
            }

            if (brushSound != null) {
                brushSound.refreshFade();
            }
        } else if (isCarryingCanvas) {
            updateCanvasPos(mouseX - lastMouseX, mouseY - lastMouseY);
        } else if (isCarryingPalette) {
            super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
            updatePalettePos(mouseX - lastMouseX, mouseY - lastMouseY);
            return;
        }
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    private void updateCanvasPos(double deltaX, double deltaY) {
        canvasX += deltaX;
        canvasY += deltaY;

        brushMeterX = (int) canvasX + canvasWidth + 2;
        brushMeterY = (int) canvasY + canvasHeight / 2 + 30;

        brushOpacityMeterX = (int) canvasX + canvasWidth + 2;
        brushOpacityMeterY = (int) canvasY;

        canvasXs[canvasType.ordinal()] = canvasX;
        canvasYs[canvasType.ordinal()] = canvasY;
    }

    private void updatePalettePos(double deltaX, double deltaY) {
        paletteX += deltaX;
        paletteY += deltaY;

        paletteXs[canvasType.ordinal()] = paletteX;
        paletteYs[canvasType.ordinal()] = paletteY;
    }

    private boolean inCanvas(int x, int y) {
        return x < canvasX + canvasWidth && x >= canvasX && y < canvasY + canvasHeight && y >= canvasY;
    }

    private boolean inCanvasHolder(int x, int y) {
        return x < canvasX + ((double) canvasWidth) * 0.75 && x >= canvasX + ((double) canvasWidth) * 0.25 && y < canvasY && y >= canvasY - canvasHolderHeight;
    }

    private boolean inBrushMeter(int x, int y) {
        return x < brushMeterX + brushSpriteSize && x >= brushMeterX && y < brushMeterY + brushSpriteSize * 4 && y >= brushMeterY;
    }

    private boolean inBrushOpacityMeter(int x, int y) {
        return x < brushOpacityMeterX + brushOpacitySpriteSize && x >= brushOpacityMeterX && y < brushOpacityMeterY + brushOpacitySpriteSize * 4 + 3 && y >= brushOpacityMeterY;
    }

    @Override
    public void onGuiClosed() {
        updateCanvas(true);
        if (dirty) {
            version++;
            CanvasUpdatePacket pack = new CanvasUpdatePacket(pixels, isSigned, canvasTitle, name, version, easel, customColors, canvasType);
            XercaPaint.network.sendToServer(pack);
        }
    }

    private void updateCanvas(boolean closing) {
        if (closing) {
            if (dirty) {
                version++;
                CanvasUpdatePacket pack = new CanvasUpdatePacket(pixels, isSigned, canvasTitle, name, version, easel, customColors, canvasType);
                XercaPaint.network.sendToServer(pack);
            } else if (easel != null) {
                EaselLeftPacket pack = new EaselLeftPacket(easel);
                XercaPaint.network.sendToServer(pack);
            }
        } else {
            if (dirty) {
                if (timeSinceLastUpdate < 10) {
                    skippedUpdate = true;
                } else {
                    version++;
                    CanvasMiniUpdatePacket pack = new CanvasMiniUpdatePacket(pixels, name, version, easel, canvasType);
                    XercaPaint.network.sendToServer(pack);
                    dirty = false;
                    timeSinceLastUpdate = 0;
                }
            }
        }
    }

    // TODO
//    public static class ToggleHelpButton extends Button {
//        protected final ResourceLocation resourceLocation;
//        protected int xTexStart;
//        protected int yTexStart;
//        protected final int yDiffText;
//        protected final int texWidth;
//        protected final int texHeight;
//
//        public ToggleHelpButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation texture, int texWidth, int texHeight, IPressable onClick, ITooltip onTooltip) {
//            super(x, y, width, height, StringTextComponent.EMPTY, onClick, onTooltip);
//            this.texWidth = texWidth;
//            this.texHeight = texHeight;
//            this.xTexStart = xTexStart;
//            this.yTexStart = yTexStart;
//            this.yDiffText = yDiffText;
//            this.resourceLocation = texture;
//        }
//
//        public void setTexStarts(int x, int y) {
//            this.xTexStart = x;
//            this.yTexStart = y;
//        }
//
//        protected void postRender(){
//            GlStateManager._enableDepthTest();
//        }
//
//        @Override
//        public void renderButton(MatrixStack matrixStack, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
//            Minecraft.getInstance().textureManager.bind(this.resourceLocation);
//            GlStateManager._disableDepthTest();
//            int yTexStartNew = this.yTexStart;
//            if (this.isHovered()) {
//                yTexStartNew += this.yDiffText;
//            }
//            int xTexStartNew = this.xTexStart + (showHelp ? 0 : this.width);
//            blit(matrixStack, this.x, this.y, (float)xTexStartNew, (float)yTexStartNew, this.width, this.height, this.texWidth, this.texHeight);
//            if (this.isHovered()) {
//                this.renderToolTip(matrixStack, p_230431_2_, p_230431_3_);
//            }
//            postRender();
//        }
//    }
}