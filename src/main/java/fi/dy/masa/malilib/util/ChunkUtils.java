package fi.dy.masa.malilib.util;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.Chunk;

public class ChunkUtils {
    /**
     * Replaces getHighestNonEmptySectionYOffset() marked for removal from Minecraft and used across downstream mods
     * Returns Maximum Y Offset Value of a Chunk.
     */
    public static int getHighestSectionYOffset(Chunk chunk)
    {
        int yMax = chunk.getHighestNonEmptySection();

        yMax = yMax == -1 ? chunk.getBottomY() : ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(yMax));

        return yMax;
    }
}
