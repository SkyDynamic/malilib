package fi.dy.masa.malilib.render;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.util.math.*;
import org.joml.*;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.random.LocalRandom;

import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.PositionUtils;
import fi.dy.masa.malilib.util.PositionUtils.HitPart;

public class RenderUtils
{
    public static final Identifier TEXTURE_MAP_BACKGROUND = new Identifier("textures/map/map_background.png");
    public static final Identifier TEXTURE_MAP_BACKGROUND_CHECKERBOARD = new Identifier("textures/map/map_background_checkerboard.png");

    private static final LocalRandom RAND = new LocalRandom(0);
    //private static final Vec3d LIGHT0_POS = (new Vec3d( 0.2D, 1.0D, -0.7D)).normalize();
    //private static final Vec3d LIGHT1_POS = (new Vec3d(-0.2D, 1.0D,  0.7D)).normalize();

    public static void setupBlend()
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
    }

    public static void setupBlendSimple()
    {
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void bindTexture(Identifier texture)
    {
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void color(float r, float g, float b, float a)
    {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void disableDiffuseLighting()
    {
        // FIXME 1.15-pre4+
        DiffuseLighting.disableGuiDepthLighting();
    }

    public static void enableDiffuseLightingForLevel()
    {
        // Uses Matrix4fStack method now
        DiffuseLighting.enableForLevel();
    }

    public static void enableDiffuseLightingGui3D()
    {
        // FIXME 1.15-pre4+
        DiffuseLighting.enableGuiDepthLighting();
    }

    public static void drawOutlinedBox(int x, int y, int width, int height, int colorBg, int colorBorder)
    {
        drawOutlinedBox(x, y, width, height, colorBg, colorBorder, 0f);
    }

    public static void drawOutlinedBox(int x, int y, int width, int height, int colorBg, int colorBorder, float zLevel)
    {
        // Draw the background
        drawRect(x, y, width, height, colorBg, zLevel);

        // Draw the border
        drawOutline(x - 1, y - 1, width + 2, height + 2, colorBorder, zLevel);
    }

    public static void drawOutline(int x, int y, int width, int height, int colorBorder)
    {
        drawOutline(x, y, width, height, colorBorder, 0f);
    }

    public static void drawOutline(int x, int y, int width, int height, int colorBorder, float zLevel)
    {
        drawRect(x                    , y,      1, height, colorBorder, zLevel); // left edge
        drawRect(x + width - 1        , y,      1, height, colorBorder, zLevel); // right edge
        drawRect(x + 1,              y, width - 2,      1, colorBorder, zLevel); // top edge
        drawRect(x + 1, y + height - 1, width - 2,      1, colorBorder, zLevel); // bottom edge
    }

    public static void drawOutline(int x, int y, int width, int height, int borderWidth, int colorBorder)
    {
        drawOutline(x, y, width, height, borderWidth, colorBorder, 0f);
    }

    public static void drawOutline(int x, int y, int width, int height, int borderWidth, int colorBorder, float zLevel)
    {
        drawRect(x                      ,                        y, borderWidth            , height     , colorBorder, zLevel); // left edge
        drawRect(x + width - borderWidth,                        y, borderWidth            , height     , colorBorder, zLevel); // right edge
        drawRect(x + borderWidth        ,                        y, width - 2 * borderWidth, borderWidth, colorBorder, zLevel); // top edge
        drawRect(x + borderWidth        , y + height - borderWidth, width - 2 * borderWidth, borderWidth, colorBorder, zLevel); // bottom edge
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height)
    {
        drawTexturedRect(x, y, u, v, width, height, 0);
    }

    public static void drawRect(int x, int y, int width, int height, int color)
    {
        drawRect(x, y, width, height, color, 0f);
    }

    public static void drawRect(int x, int y, int width, int height, int color, float zLevel)
    {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >>  8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.applyModelViewMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        setupBlend();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(x        , y         , zLevel).color(r, g, b, a).next();
        buffer.vertex(x        , y + height, zLevel).color(r, g, b, a).next();
        buffer.vertex(x + width, y + height, zLevel).color(r, g, b, a).next();
        buffer.vertex(x + width, y         , zLevel).color(r, g, b, a).next();

        tessellator.draw();

        RenderSystem.disableBlend();
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, float zLevel)
    {
        float pixelWidth = 0.00390625F;
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.applyModelViewMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        setupBlend();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        buffer.vertex(x        , y + height, zLevel).texture( u          * pixelWidth, (v + height) * pixelWidth).next();
        buffer.vertex(x + width, y + height, zLevel).texture((u + width) * pixelWidth, (v + height) * pixelWidth).next();
        buffer.vertex(x + width, y         , zLevel).texture((u + width) * pixelWidth,  v           * pixelWidth).next();
        buffer.vertex(x        , y         , zLevel).texture( u          * pixelWidth,  v           * pixelWidth).next();

        tessellator.draw();
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, BufferBuilder buffer)
    {
        drawTexturedRectBatched(x, y, u, v, width, height, 0, buffer);
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder buffer)
    {
        float pixelWidth = 0.00390625F;

        buffer.vertex(x        , y + height, zLevel).texture( u          * pixelWidth, (v + height) * pixelWidth).next();
        buffer.vertex(x + width, y + height, zLevel).texture((u + width) * pixelWidth, (v + height) * pixelWidth).next();
        buffer.vertex(x + width, y         , zLevel).texture((u + width) * pixelWidth,  v           * pixelWidth).next();
        buffer.vertex(x        , y         , zLevel).texture( u          * pixelWidth,  v           * pixelWidth).next();
    }

    public static void drawHoverText(int x, int y, List<String> textLines, DrawContext drawContext)
    {
        MinecraftClient mc = mc();

        if (!textLines.isEmpty() && GuiUtils.getCurrentScreen() != null)
        {
            RenderSystem.enableDepthTest();
            TextRenderer font = mc.textRenderer;
            int maxLineLength = 0;
            int maxWidth = GuiUtils.getCurrentScreen().width;
            List<String> linesNew = new ArrayList<>();

            for (String lineOrig : textLines)
            {
                String[] lines = lineOrig.split("\\n");

                for (String line : lines)
                {
                    int length = font.getWidth(line);

                    if (length > maxLineLength)
                    {
                        maxLineLength = length;
                    }

                    linesNew.add(line);
                }
            }

            textLines = linesNew;

            final int lineHeight = font.fontHeight + 1;
            int textHeight = textLines.size() * lineHeight - 2;
            int textStartX = x + 4;
            int textStartY = Math.max(8, y - textHeight - 6);

            if (textStartX + maxLineLength + 6 > maxWidth)
            {
                textStartX = Math.max(2, maxWidth - maxLineLength - 8);
            }

            // TODO -- DrawContext still uses MatrixStack,
            //  so I am sure Mojang will change this in the future
            MatrixStack matrixStack = drawContext.getMatrices();
            matrixStack.push();
            matrixStack.translate(0, 0, 300);
            RenderSystem.applyModelViewMatrix();

            double zLevel = 300;
            int borderColor = 0xF0100010;
            drawGradientRect(textStartX - 3, textStartY - 4, textStartX + maxLineLength + 3, textStartY - 3, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX - 3, textStartY + textHeight + 3, textStartX + maxLineLength + 3, textStartY + textHeight + 4, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX - 3, textStartY - 3, textStartX + maxLineLength + 3, textStartY + textHeight + 3, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX - 4, textStartY - 3, textStartX - 3, textStartY + textHeight + 3, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX + maxLineLength + 3, textStartY - 3, textStartX + maxLineLength + 4, textStartY + textHeight + 3, zLevel, borderColor, borderColor);

            int fillColor1 = 0x505000FF;
            int fillColor2 = 0x5028007F;
            drawGradientRect(textStartX - 3, textStartY - 3 + 1, textStartX - 3 + 1, textStartY + textHeight + 3 - 1, zLevel, fillColor1, fillColor2);
            drawGradientRect(textStartX + maxLineLength + 2, textStartY - 3 + 1, textStartX + maxLineLength + 3, textStartY + textHeight + 3 - 1, zLevel, fillColor1, fillColor2);
            drawGradientRect(textStartX - 3, textStartY - 3, textStartX + maxLineLength + 3, textStartY - 3 + 1, zLevel, fillColor1, fillColor1);
            drawGradientRect(textStartX - 3, textStartY + textHeight + 2, textStartX + maxLineLength + 3, textStartY + textHeight + 3, zLevel, fillColor2, fillColor2);

            for (int i = 0; i < textLines.size(); ++i)
            {
                String str = textLines.get(i);

                drawContext.drawText(font, str, textStartX, textStartY, 0xFFFFFFFF, false);
                textStartY += lineHeight;
            }

            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();

            //RenderSystem.enableDepthTest();
            //enableDiffuseLightingGui3D();
        }
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, double zLevel, int startColor, int endColor)
    {
        int sa = (startColor >> 24 & 0xFF);
        int sr = (startColor >> 16 & 0xFF);
        int sg = (startColor >>  8 & 0xFF);
        int sb = (startColor & 0xFF);

        int ea = (endColor >> 24 & 0xFF);
        int er = (endColor >> 16 & 0xFF);
        int eg = (endColor >>  8 & 0xFF);
        int eb = (endColor & 0xFF);

        setupBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.applyModelViewMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(right, top,    zLevel).color(sr, sg, sb, sa).next();
        buffer.vertex(left,  top,    zLevel).color(sr, sg, sb, sa).next();
        buffer.vertex(left,  bottom, zLevel).color(er, eg, eb, ea).next();
        buffer.vertex(right, bottom, zLevel).color(er, eg, eb, ea).next();

        tessellator.draw();

        RenderSystem.disableBlend();
    }

    public static void drawCenteredString(int x, int y, int color, String text, DrawContext drawContext)
    {
        TextRenderer textRenderer = mc().textRenderer;
        drawContext.drawCenteredTextWithShadow(textRenderer, text, x, y, color);
    }

    public static void drawHorizontalLine(int x, int y, int width, int color)
    {
        drawRect(x, y, width, 1, color);
    }

    public static void drawVerticalLine(int x, int y, int height, int color)
    {
        drawRect(x, y, 1, height, color);
    }

    public static void renderSprite(int x, int y, int width, int height, Identifier atlas, Identifier texture, DrawContext drawContext)
    {
        if (texture != null)
        {
            Sprite sprite = mc().getSpriteAtlas(atlas).apply(texture);
            drawContext.drawSprite(x, y, 0, width, height, sprite);
        }
    }

    public static void renderText(int x, int y, int color, String text, DrawContext drawContext)
    {
        String[] parts = text.split("\\\\n");
        TextRenderer textRenderer = mc().textRenderer;

        for (String line : parts)
        {
            drawContext.drawText(textRenderer, line, x, y, color, true);
            y += textRenderer.fontHeight + 1;
        }
    }

    public static void renderText(int x, int y, int color, List<String> lines, DrawContext drawContext)
    {
        if (!lines.isEmpty())
        {
            TextRenderer textRenderer = mc().textRenderer;

            for (String line : lines)
            {
                drawContext.drawText(textRenderer, line, x, y, color, false);
                y += textRenderer.fontHeight + 2;
            }
        }
    }

    public static int renderText(int xOff, int yOff, double scale, int textColor, int bgColor,
            HudAlignment alignment, boolean useBackground, boolean useShadow, List<String> lines,
            DrawContext drawContext)
    {
        TextRenderer fontRenderer = mc().textRenderer;
        final int scaledWidth = GuiUtils.getScaledWindowWidth();
        final int lineHeight = fontRenderer.fontHeight + 2;
        final int contentHeight = lines.size() * lineHeight - 2;
        final int bgMargin = 2;

        // Only Chuck Norris can divide by zero
        if (scale < 0.0125)
        {
            return 0;
        }

        // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
        Matrix4fStack global4fStack = RenderSystem.getModelViewStack();
        boolean scaled = scale != 1.0;

        if (scaled)
        {
            if (scale != 0)
            {
                xOff = (int) (xOff * scale);
                yOff = (int) (yOff * scale);
            }

            global4fStack.pushMatrix();
            global4fStack.scale((float) scale, (float) scale, 1.0f);
            RenderSystem.applyModelViewMatrix();
        }

        double posX = xOff + bgMargin;
        double posY = yOff + bgMargin;

        posY = getHudPosY((int) posY, yOff, contentHeight, scale, alignment);
        posY += getHudOffsetForPotions(alignment, scale, mc().player);

        for (String line : lines)
        {
            final int width = fontRenderer.getWidth(line);

            switch (alignment)
            {
                case TOP_RIGHT:
                case BOTTOM_RIGHT:
                    posX = (scaledWidth / scale) - width - xOff - bgMargin;
                    break;
                case CENTER:
                    posX = (scaledWidth / scale / 2) - (width / 2) - xOff;
                    break;
                default:
            }

            final int x = (int) posX;
            final int y = (int) posY;
            posY += lineHeight;

            if (useBackground)
            {
                drawRect(x - bgMargin, y - bgMargin, width + bgMargin, bgMargin + fontRenderer.fontHeight, bgColor);
            }

            drawContext.drawText(fontRenderer, line, x, y, textColor, useShadow);
        }

        if (scaled)
        {
            global4fStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        return contentHeight + bgMargin * 2;
    }

    public static int getHudOffsetForPotions(HudAlignment alignment, double scale, PlayerEntity player)
    {
        if (alignment == HudAlignment.TOP_RIGHT)
        {
            // Only Chuck Norris can divide by zero
            if (scale == 0d)
            {
                return 0;
            }

            Collection<StatusEffectInstance> effects = player.getStatusEffects();

            if (!effects.isEmpty())
            {
                int y1 = 0;
                int y2 = 0;

                for (StatusEffectInstance effectInstance : effects)
                {
                    StatusEffect effect = effectInstance.getEffectType().value();

                    if (effectInstance.shouldShowParticles() && effectInstance.shouldShowIcon())
                    {
                        if (effect.isBeneficial())
                        {
                            y1 = 26;
                        }
                        else
                        {
                            y2 = 52;
                            break;
                        }
                    }
                }

                return (int) (Math.max(y1, y2) / scale);
            }
        }

        return 0;
    }

    public static int getHudPosY(int yOrig, int yOffset, int contentHeight, double scale, HudAlignment alignment)
    {
        int scaledHeight = GuiUtils.getScaledWindowHeight();
        int posY = yOrig;

        switch (alignment)
        {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                posY = (int) ((scaledHeight / scale) - contentHeight - yOffset);
                break;
            case CENTER:
                posY = (int) ((scaledHeight / scale / 2.0d) - (contentHeight / 2.0d) + yOffset);
                break;
            default:
        }

        return posY;
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBlockBoundingBoxSidesBatchedQuads(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        double minX = pos.getX() - expand;
        double minY = pos.getY() - expand;
        double minZ = pos.getZ() - expand;
        double maxX = pos.getX() + expand + 1;
        double maxY = pos.getY() + expand + 1;
        double maxZ = pos.getZ() + expand + 1;

        drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBlockBoundingBoxOutlinesBatchedLines(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        drawBlockBoundingBoxOutlinesBatchedLines(pos, Vec3d.ZERO, color, expand, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized.
     * The cameraPos value will be subtracted from the absolute coordinate values of the passed in BlockPos.
     */
    public static void drawBlockBoundingBoxOutlinesBatchedLines(BlockPos pos, Vec3d cameraPos, Color4f color, double expand, BufferBuilder buffer)
    {
        double minX = pos.getX() - expand - cameraPos.x;
        double minY = pos.getY() - expand - cameraPos.y;
        double minZ = pos.getZ() - expand - cameraPos.z;
        double maxX = pos.getX() + expand - cameraPos.x + 1;
        double maxY = pos.getY() + expand - cameraPos.y + 1;
        double maxZ = pos.getZ() + expand - cameraPos.z + 1;

        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxAllSidesBatchedQuads(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        drawBoxHorizontalSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
        drawBoxTopBatchedQuads(minX, minZ, maxX, maxY, maxZ, color, buffer);
        drawBoxBottomBatchedQuads(minX, minY, minZ, maxX, maxZ, color, buffer);
    }

    /**
     * Draws a box with outlines around the given corner positions.
     * Takes in buffers initialized for GL_QUADS and GL_LINES modes.
     */
    public static void drawBoxWithEdgesBatched(BlockPos posMin, BlockPos posMax, Color4f colorLines, Color4f colorSides, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        drawBoxWithEdgesBatched(posMin, posMax, Vec3d.ZERO, colorLines, colorSides, bufferQuads, bufferLines);
    }

    /**
     * Draws a box with outlines around the given corner positions.
     * Takes in buffers initialized for GL_QUADS and GL_LINES modes.
     * The cameraPos value will be subtracted from the absolute coordinate values of the passed in block positions.
     */
    public static void drawBoxWithEdgesBatched(BlockPos posMin, BlockPos posMax, Vec3d cameraPos, Color4f colorLines, Color4f colorSides, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        final double x1 = posMin.getX() - cameraPos.x;
        final double y1 = posMin.getY() - cameraPos.y;
        final double z1 = posMin.getZ() - cameraPos.z;
        final double x2 = posMax.getX() + 1 - cameraPos.x;
        final double y2 = posMax.getY() + 1 - cameraPos.y;
        final double z2 = posMax.getZ() + 1 - cameraPos.z;

        RenderUtils.drawBoxAllSidesBatchedQuads(x1, y1, z1, x2, y2, z2, colorSides, bufferQuads);
        RenderUtils.drawBoxAllEdgesBatchedLines(x1, y1, z1, x2, y2, z2, colorLines, bufferLines);
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxHorizontalSidesBatchedQuads(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        // West side
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();

        // East side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();

        // North side
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();

        // South side
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxTopBatchedQuads(double minX, double minZ, double maxX, double maxY, double maxZ, Color4f color, BufferBuilder buffer)
    {
        // Top side
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxBottomBatchedQuads(double minX, double minY, double minZ, double maxX, double maxZ, Color4f color, BufferBuilder buffer)
    {
        // Bottom side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBoxAllEdgesBatchedLines(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        // West side
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();

        // East side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();

        // North side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();

        // South side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
    }

    public static void drawBox(IntBoundingBox bb, Vec3d cameraPos, Color4f color, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        double minX = bb.minX - cameraPos.x;
        double minY = bb.minY - cameraPos.y;
        double minZ = bb.minZ - cameraPos.z;
        double maxX = bb.maxX + 1 - cameraPos.x;
        double maxY = bb.maxY + 1 - cameraPos.y;
        double maxZ = bb.maxZ + 1 - cameraPos.z;

        drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, bufferQuads);
        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, bufferLines);
    }

    /**
     * Renders a text plate/billboard, similar to the player name plate.<br>
     * The plate will always face towards the viewer.
     */
    public static void drawTextPlate(List<String> text, double x, double y, double z, float scale)
    {
        Entity entity = mc().getCameraEntity();

        if (entity != null)
        {
            drawTextPlate(text, x, y, z, entity.getYaw(), entity.getPitch(), scale, 0xFFFFFFFF, 0x40000000, true);
        }
    }

    public static void drawTextPlate(List<String> text, double x, double y, double z, float yaw, float pitch,
                                     float scale, int textColor, int bgColor, boolean disableDepth)
    {
        Vec3d cameraPos = mc().gameRenderer.getCamera().getPos();
        double cx = cameraPos.x;
        double cy = cameraPos.y;
        double cz = cameraPos.z;
        TextRenderer textRenderer = mc().textRenderer;

        // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
        Matrix4fStack global4fStack = RenderSystem.getModelViewStack();
        global4fStack.pushMatrix();

        global4fStack.translate((float) (x - cx), (float) (y - cy), (float) (z - cz));

        // TODO Matrix4f handles the rotations directly, but watch for invalid rotation values.
        //  Wrap it with matrix4fRotateFix() if rotation errors are found.
        //Quaternionf rot = new Quaternionf().rotationYXZ(-yaw * (float) (Math.PI / 180.0), pitch * (float) (Math.PI / 180.0), 0.0F);

        global4fStack.rotateYXZ((-yaw) * ((float) (Math.PI / 180.0)), pitch * ((float) (Math.PI / 180.0)), 0.0F);

        global4fStack.scale((-scale), (-scale), scale);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableCull();

        setupBlend();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int maxLineLen = 0;

        for (String line : text)
        {
            maxLineLen = Math.max(maxLineLen, textRenderer.getWidth(line));
        }

        int strLenHalf = maxLineLen / 2;
        int textHeight = textRenderer.fontHeight * text.size() - 1;
        int bga = ((bgColor >>> 24) & 0xFF);
        int bgr = ((bgColor >>> 16) & 0xFF);
        int bgg = ((bgColor >>>  8) & 0xFF);
        int bgb = (bgColor          & 0xFF);

        if (disableDepth)
        {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(-strLenHalf - 1,          -1, 0.0D).color(bgr, bgg, bgb, bga).next();
        buffer.vertex(-strLenHalf - 1,  textHeight, 0.0D).color(bgr, bgg, bgb, bga).next();
        buffer.vertex( strLenHalf    ,  textHeight, 0.0D).color(bgr, bgg, bgb, bga).next();
        buffer.vertex( strLenHalf    ,          -1, 0.0D).color(bgr, bgg, bgb, bga).next();
        tessellator.draw();

        int textY = 0;

        // translate the text a bit infront of the background
        if (!disableDepth)
        {
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-0.6f, -1.2f);
        }

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity();

        for (String line : text)
        {
            if (disableDepth)
            {
                RenderSystem.depthMask(false);
                RenderSystem.disableDepthTest();
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(buffer);
                textRenderer.draw(line, -strLenHalf, textY, 0x20000000 | (textColor & 0xFFFFFF), false, modelMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
                immediate.draw();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
            }

            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(buffer);
            textRenderer.draw(line, -strLenHalf, textY, textColor, false, modelMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
            immediate.draw();
            textY += textRenderer.fontHeight;
        }

        if (!disableDepth)
        {
            RenderSystem.polygonOffset(0f, 0f);
            RenderSystem.disablePolygonOffset();
        }

        color(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        global4fStack.popMatrix();
    }

    public static void renderBlockTargetingOverlay(Entity entity, BlockPos pos, Direction side, Vec3d hitVec,
            Color4f color, Matrix4f matrix4f, MinecraftClient mc)
    {
        Direction playerFacing = entity.getHorizontalFacing();
        HitPart part = PositionUtils.getHitPart(side, playerFacing, pos, hitVec);
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        double x = pos.getX() + 0.5d - cameraPos.x;
        double y = pos.getY() + 0.5d - cameraPos.y;
        double z = pos.getZ() + 0.5d - cameraPos.z;

        // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
        Matrix4fStack global4fStack = RenderSystem.getModelViewStack();
        global4fStack.pushMatrix();
        blockTargetingOverlayTranslations(x, y, z, side, playerFacing, global4fStack);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int quadAlpha = (int) (0.18f * 255f);
        int hr = (int) (color.r * 255f);
        int hg = (int) (color.g * 255f);
        int hb = (int) (color.b * 255f);
        int ha = (int) (color.a * 255f);
        int c = 255;

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // White full block background
        buffer.vertex(x - 0.5, y - 0.5, z).color(c, c, c, quadAlpha).next();
        buffer.vertex(x + 0.5, y - 0.5, z).color(c, c, c, quadAlpha).next();
        buffer.vertex(x + 0.5, y + 0.5, z).color(c, c, c, quadAlpha).next();
        buffer.vertex(x - 0.5, y + 0.5, z).color(c, c, c, quadAlpha).next();

        switch (part)
        {
            case CENTER:
                buffer.vertex(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).next();
                break;
            case LEFT:
                buffer.vertex(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).next();
                break;
            case RIGHT:
                buffer.vertex(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).next();
                break;
            case TOP:
                buffer.vertex(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).next();
                break;
            case BOTTOM:
                buffer.vertex(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).next();
                buffer.vertex(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).next();
                break;
            default:
        }

        tessellator.draw();

        // FIXME: line width doesn't work currently
        RenderSystem.lineWidth(1.6f);

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        // Middle small rectangle
        buffer.vertex(x - 0.25, y - 0.25, z).color(c, c, c, c).next();
        buffer.vertex(x + 0.25, y - 0.25, z).color(c, c, c, c).next();
        buffer.vertex(x + 0.25, y + 0.25, z).color(c, c, c, c).next();
        buffer.vertex(x - 0.25, y + 0.25, z).color(c, c, c, c).next();
        buffer.vertex(x - 0.25, y - 0.25, z).color(c, c, c, c).next();
        tessellator.draw();

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        // Bottom left
        buffer.vertex(x - 0.50, y - 0.50, z).color(c, c, c, c).next();
        buffer.vertex(x - 0.25, y - 0.25, z).color(c, c, c, c).next();

        // Top left
        buffer.vertex(x - 0.50, y + 0.50, z).color(c, c, c, c).next();
        buffer.vertex(x - 0.25, y + 0.25, z).color(c, c, c, c).next();

        // Bottom right
        buffer.vertex(x + 0.50, y - 0.50, z).color(c, c, c, c).next();
        buffer.vertex(x + 0.25, y - 0.25, z).color(c, c, c, c).next();

        // Top right
        buffer.vertex(x + 0.50, y + 0.50, z).color(c, c, c, c).next();
        buffer.vertex(x + 0.25, y + 0.25, z).color(c, c, c, c).next();
        tessellator.draw();

        global4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderBlockTargetingOverlaySimple(Entity entity, BlockPos pos, Direction side,
            Color4f color, Matrix4f matrix4f, MinecraftClient mc)
    {
        Direction playerFacing = entity.getHorizontalFacing();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        double x = pos.getX() + 0.5d - cameraPos.x;
        double y = pos.getY() + 0.5d - cameraPos.y;
        double z = pos.getZ() + 0.5d - cameraPos.z;

        // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
        Matrix4fStack global4fStack = RenderSystem.getModelViewStack();
        global4fStack.pushMatrix();

        blockTargetingOverlayTranslations(x, y, z, side, playerFacing, global4fStack);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int a = (int) (color.a * 255f);
        int r = (int) (color.r * 255f);
        int g = (int) (color.g * 255f);
        int b = (int) (color.b * 255f);
        int c = 255;

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Simple colored quad
        buffer.vertex(x - 0.5, y - 0.5, z).color(r, g, b, a).next();
        buffer.vertex(x + 0.5, y - 0.5, z).color(r, g, b, a).next();
        buffer.vertex(x + 0.5, y + 0.5, z).color(r, g, b, a).next();
        buffer.vertex(x - 0.5, y + 0.5, z).color(r, g, b, a).next();

        tessellator.draw();

        // FIXME: line width doesn't work currently
        RenderSystem.lineWidth(1.6f);

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        // Middle rectangle
        buffer.vertex(x - 0.375, y - 0.375, z).color(c, c, c, c).next();
        buffer.vertex(x + 0.375, y - 0.375, z).color(c, c, c, c).next();
        buffer.vertex(x + 0.375, y + 0.375, z).color(c, c, c, c).next();
        buffer.vertex(x - 0.375, y + 0.375, z).color(c, c, c, c).next();

        tessellator.draw();

        global4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * Matrix4f rotation adds direct values without adding these numbers.
     * --> fix with matrix4fRotateFix()
     * *****************************************************
     * (VIA RotationAxis.class)
     * default Quaternionf rotationDegrees(float deg) {
     *         return this.rotation(deg * 0.017453292F);
     * *****************************************************
     */
    private static void blockTargetingOverlayTranslations(double x, double y, double z,
            Direction side, Direction playerFacing, Matrix4fStack matrix4fStack)
    {
        matrix4fStack.translate((float) x, (float) y, (float) z);

        switch (side)
        {
            case DOWN:
                matrix4fStack.rotateY(matrix4fRotateFix(180f - playerFacing.asRotation()));
                matrix4fStack.rotateX(matrix4fRotateFix(90f));
                break;
            case UP:
                matrix4fStack.rotateY(matrix4fRotateFix(180f - playerFacing.asRotation()));
                matrix4fStack.rotateX(matrix4fRotateFix(-90f));
                break;
            case NORTH:
                matrix4fStack.rotateY(matrix4fRotateFix(180f));
                break;
            case SOUTH:
                break;
            case WEST:
                matrix4fStack.rotateY(matrix4fRotateFix(-90f));
                break;
            case EAST:
                matrix4fStack.rotateY(matrix4fRotateFix(90f));
                break;
        }

        matrix4fStack.translate((float) (-x), (float) (-y), (float) ((-z) + 0.510));
    }

    public static void renderMapPreview(ItemStack stack, int x, int y, int dimensions)
    {
        renderMapPreview(stack, x, y, dimensions, true);
    }

    public static void renderMapPreview(ItemStack stack, int x, int y, int dimensions, boolean requireShift)
    {
        if (stack.getItem() instanceof FilledMapItem && (!requireShift || GuiBase.isShiftDown()))
        {
            color(1f, 1f, 1f, 1f);

            int y1 = y - dimensions - 20;
            int y2 = y1 + dimensions;
            int x1 = x + 8;
            int x2 = x1 + dimensions;
            int z = 300;

            MapState mapState = FilledMapItem.getMapState(stack, mc().world);
            ComponentMap data = stack.getComponents();
            MapIdComponent mapId = data.get(DataComponentTypes.MAP_ID);

            Identifier bgTexture = mapState == null ? TEXTURE_MAP_BACKGROUND : TEXTURE_MAP_BACKGROUND_CHECKERBOARD;
            bindTexture(bgTexture);
            setupBlend();

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.applyModelViewMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(x1, y2, z).texture(0.0f, 1.0f).next();
            buffer.vertex(x2, y2, z).texture(1.0f, 1.0f).next();
            buffer.vertex(x2, y1, z).texture(1.0f, 0.0f).next();
            buffer.vertex(x1, y1, z).texture(0.0f, 0.0f).next();
            tessellator.draw();
            RenderSystem.disableBlend();

            if (mapState != null)
            {
                x1 += 8;
                y1 += 8;
                z = 310;
                VertexConsumerProvider.Immediate consumer = VertexConsumerProvider.immediate(buffer);
                double scale = (double) (dimensions - 16) / 128.0D;

                // TODO -- MapRenderer still uses MatrixStack,
                //  which I'm sure Mojang will change in the future
                MatrixStack matrixStack = new MatrixStack();
                matrixStack.push();
                matrixStack.translate(x1, y1, z);
                matrixStack.scale((float) scale, (float) scale, 0);
                mc().gameRenderer.getMapRenderer().draw(matrixStack, consumer, mapId, mapState, false, 0xF000F0);
                consumer.draw();
                matrixStack.pop();
            }
        }
    }

    public static void renderBundlePreview(ItemStack stack, int baseX, int baseY, Color4f bg, DrawContext drawContext)
    {
        if (stack.getComponents() != ComponentMap.EMPTY)
        {
            int occupancy = InventoryUtils.bundleCountItems(stack);
            if (occupancy < 1)
            {
                // Occupants is the total number of items up to 64, but items.size() is the number of slots.
                return;
            }

            DefaultedList<ItemStack> items = InventoryUtils.getBundleItems(stack);

            if (items.isEmpty())
            {
                return;
            }
            if (items.size() > 27)
            {
                // Disable this if the Slots in the Bundle are too much for the screen to logically display
                //  The Double-Chest (54) Type also doesn't look very good either,
                //  but I would assume most use cases are less than 27 different items.
                items.clear();
                return;
            }

            InventoryOverlay.InventoryRenderType type = InventoryOverlay.getInventoryType(stack);
            InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, items.size());

            int screenWidth = GuiUtils.getScaledWindowWidth();
            int screenHeight = GuiUtils.getScaledWindowHeight();
            int height = props.height + 18;
            int x = MathHelper.clamp(baseX + 8     , 0, screenWidth - props.width);
            int y = MathHelper.clamp(baseY - height, 0, screenHeight - height);

            color(bg.r, bg.g, bg.b, 1f);

            disableDiffuseLighting();

            // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.translate(0, 0, 500);
            RenderSystem.applyModelViewMatrix();

            InventoryOverlay.renderInventoryBackground(type, x, y, props.slotsPerRow, items.size(), mc());

            enableDiffuseLightingGui3D();

            Inventory inv = InventoryUtils.getAsInventory(items);
            InventoryOverlay.renderInventoryStacks(type, inv, x + props.slotOffsetX, y + props.slotOffsetY, props.slotsPerRow, 0, items.size(), mc(), drawContext);

            matrix4fStack.popMatrix();
            RenderSystem.applyModelViewMatrix();

            items.clear();
        }
    }
    public static void renderShulkerBoxPreview(ItemStack stack, int baseX, int baseY, boolean useBgColors, DrawContext drawContext)
    {
        if (stack.getComponents() != ComponentMap.EMPTY)
        {
            DefaultedList<ItemStack> items = InventoryUtils.getStoredItems(stack, ShulkerBoxBlockEntity.INVENTORY_SIZE);

            if (items.isEmpty())
            {
                return;
            }

            InventoryOverlay.InventoryRenderType type = InventoryOverlay.getInventoryType(stack);
            InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, items.size());

            int screenWidth = GuiUtils.getScaledWindowWidth();
            int screenHeight = GuiUtils.getScaledWindowHeight();
            int height = props.height + 18;
            int x = MathHelper.clamp(baseX + 8     , 0, screenWidth - props.width);
            int y = MathHelper.clamp(baseY - height, 0, screenHeight - height);

            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock)
            {
                setShulkerboxBackgroundTintColor((ShulkerBoxBlock) ((BlockItem) stack.getItem()).getBlock(), useBgColors);
            }
            else
            {
                color(1f, 1f, 1f, 1f);
            }

            disableDiffuseLighting();

            // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.translate(0, 0, 500);
            RenderSystem.applyModelViewMatrix();

            InventoryOverlay.renderInventoryBackground(type, x, y, props.slotsPerRow, items.size(), mc());

            enableDiffuseLightingGui3D();

            Inventory inv = InventoryUtils.getAsInventory(items);
            InventoryOverlay.renderInventoryStacks(type, inv, x + props.slotOffsetX, y + props.slotOffsetY, props.slotsPerRow, 0, ShulkerBoxBlockEntity.INVENTORY_SIZE, mc(), drawContext);

            matrix4fStack.popMatrix();
            RenderSystem.applyModelViewMatrix();

            items.clear();
        }
    }

    /**
     * Calls RenderUtils.color() with the dye color of the provided shulker box block's color
     */
    public static void setShulkerboxBackgroundTintColor(@Nullable ShulkerBoxBlock block, boolean useBgColors)
    {
        if (block != null && useBgColors)
        {
            // In 1.13+ there is the uncolored Shulker Box variant, which returns null from getColor()
            final DyeColor dye = block.getColor() != null ? block.getColor() : DyeColor.PURPLE;
            final float[] colors = dye.getColorComponents();
            color(colors[0], colors[1], colors[2], 1f);
        }
        else
        {
            color(1f, 1f, 1f, 1f);
        }
    }

    public static void renderModelInGui(int x, int y, BakedModel model, BlockState state, float zLevel)
    {
        if (state.getBlock() == Blocks.AIR)
        {
            return;
        }

        // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        mc().getTextureManager().getTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).setFilter(false, false);

        RenderSystem.enableBlend();
        setupBlendSimple();
        color(1f, 1f, 1f, 1f);

        setupGuiTransform(x, y, model.hasDepth(), zLevel);
        //model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);

        // TODO -- Verify we get the correct rotations for this method using the matrix4fRotateFix(),
        //  but what uses this function to test?
        matrix4fStack.rotateX(matrix4fRotateFix(30));
        matrix4fStack.rotateY(matrix4fRotateFix(225));

        matrix4fStack.scale(0.625f, 0.625f, 0.625f);

        renderModel(model, state);
        matrix4fStack.popMatrix();
    }

    public static void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d, float zLevel)
    {
        setupGuiTransform(RenderSystem.getModelViewStack(), xPosition, yPosition, zLevel);
    }

    public static void setupGuiTransform(Matrix4fStack matrix4fStack, int xPosition, int yPosition, float zLevel)
    {
        // TODO --> RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method,
        //  verify this method works, but what is using it?
        matrix4fStack.translate((float) (xPosition + 8.0), (float) (yPosition + 8.0), (float) (zLevel + 100.0));
        matrix4fStack.scale((float) 16, (float) -16, (float) 16);
    }

    private static void renderModel(BakedModel model, BlockState state)
    {
        // RenderSystem's 'modelViewStack' was changed to a Matrix4fStack method
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();

        matrix4fStack.translate((float) -0.5, (float) -0.5, (float) -0.5);
        int color = 0xFFFFFFFF;

        if (!model.isBuiltin())
        {
            RenderSystem.setShader(GameRenderer::getRenderTypeSolidProgram);
            RenderSystem.applyModelViewMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);

            for (Direction face : Direction.values())
            {
                RAND.setSeed(0);
                renderQuads(bufferbuilder, model.getQuads(state, face, RAND), state, color);
            }

            RAND.setSeed(0);
            renderQuads(bufferbuilder, model.getQuads(state, null, RAND), state, color);
            tessellator.draw();
        }

        matrix4fStack.popMatrix();
    }

    private static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, BlockState state, int color)
    {
        final int quadCount = quads.size();

        for (int i = 0; i < quadCount; ++i)
        {
            BakedQuad quad = quads.get(i);
            renderQuad(renderer, quad, state, 0xFFFFFFFF);
        }
    }

    private static void renderQuad(BufferBuilder buffer, BakedQuad quad, BlockState state, int color)
    {
        /*
        buffer.putVertexData(quad.getVertexData());
        buffer.setQuadColor(color);

        if (quad.hasColor())
        {
            BlockColors blockColors = mc().getBlockColorMap();
            int m = blockColors.getColorMultiplier(state, null, null, quad.getColorIndex());

            float r = (float) (m >>> 16 & 0xFF) / 255F;
            float g = (float) (m >>>  8 & 0xFF) / 255F;
            float b = (float) (m        & 0xFF) / 255F;
            buffer.multiplyColor(r, g, b, 4);
            buffer.multiplyColor(r, g, b, 3);
            buffer.multiplyColor(r, g, b, 2);
            buffer.multiplyColor(r, g, b, 1);
        }

        putQuadNormal(buffer, quad);
    }

    private static void putQuadNormal(BufferBuilder renderer, BakedQuad quad)
    {
        Vec3i direction = quad.getFace().getVector();
        renderer.normal(direction.getX(), direction.getY(), direction.getZ());
        */
    }

    @Nullable
    private static MinecraftClient mc()
    {
        return MinecraftClient.getInstance();
    }

    /*
    public static void enableGUIStandardItemLighting(float scale)
    {
        RenderSystem.pushMatrix();
        RenderSystem.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotate(165.0F, 1.0F, 0.0F, 0.0F);

        enableStandardItemLighting(scale);

        RenderSystem.popMatrix();
    }

    public static void enableStandardItemLighting(float scale)
    {
        RenderSystem.enableLighting();
        RenderSystem.enableLight(0);
        RenderSystem.enableLight(1);
        RenderSystem.enableColorMaterial();
        RenderUtils.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT0_POS.x, (float) LIGHT0_POS.y, (float) LIGHT0_POS.z, 0.0f));

        float lightStrength = 0.3F * scale;
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT1_POS.x, (float) LIGHT1_POS.y, (float) LIGHT1_POS.z, 0.0f));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));

        RenderSystem.shadeModel(GL11.GL_FLAT);

        float ambientLightStrength = 0.4F;
        RenderSystem.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, RenderHelper.setColorBuffer(ambientLightStrength, ambientLightStrength, ambientLightStrength, 1.0F));
    }
    */

    /**
     * Only required for translating the values to their RotationAxis.POSITIVE_?.rotationDegrees() equivalence
     */
    public static float matrix4fRotateFix(float ang) { return (ang * 0.017453292F); }
}
