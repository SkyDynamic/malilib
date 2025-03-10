package fi.dy.masa.malilib.interfaces;

import java.util.function.Supplier;
import org.joml.Matrix4f;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public interface IRenderer
{
    /**
     * Called after the vanilla overlays have been rendered
     */
    default void onRenderGameOverlayPost(DrawContext drawContext) {}

    /**
     * Called after vanilla world rendering
     * --> Changed to Matrix4f for all downstream Mods
     */
    default void onRenderWorldLast(Matrix4f matrix4f, Matrix4f projMatrix) {}

    /**
     * Called after the tooltip text of an item has been rendered
     */
    default void onRenderTooltipLast(DrawContext drawContext,ItemStack stack, int x, int y) {}

    /**
     * Returns a supplier for the profiler section name that should be used for this renderer
     */
    default Supplier<String> getProfilerSectionSupplier()
    {
        return () -> this.getClass().getName();
    }
}
