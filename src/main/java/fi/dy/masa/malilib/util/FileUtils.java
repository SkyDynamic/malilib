package fi.dy.masa.malilib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableSet;

import fi.dy.masa.malilib.MaLiLibReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;

import fi.dy.masa.malilib.MaLiLib;

public class FileUtils
{
    private static final Set<Character> ILLEGAL_CHARACTERS = ImmutableSet.of( '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' );
    private static File runDirectory;
    private static File configDirectory;

    public static File getConfigDirectory()
    {
        if (MaLiLibReference.isClient())
            return new File(MinecraftClient.getInstance().runDirectory, "config");
        else
        {
            if (configDirectory.isDirectory())
            {
                return configDirectory;
            }
            else
                return MaLiLibReference.DEFAULT_CONFIG_DIR;
        }
    }

    public static File getMinecraftDirectory()
    {
        if (MaLiLibReference.isClient())
            return MinecraftClient.getInstance().runDirectory;
        else
        {
            if (runDirectory.isDirectory())
            {
                return runDirectory;
            }
            else
                return MaLiLibReference.DEFAULT_RUN_DIR;
        }
    }

    /**
     * Checks that the target directory exists, and the file either doesn't exist,
     * or the canOverwrite argument is true and the file is writable
     * @param dir
     * @param fileName
     * @param canOverwrite
     * @return
     */
    public static boolean canWriteToFile(File dir, String fileName, boolean canOverwrite)
    {
        if (dir.exists() && dir.isDirectory())
        {
            File file = new File(dir, fileName);
            return file.exists() == false || (canOverwrite && file.isFile() && file.canWrite());
        }

        return false;
    }

    public static File getCanonicalFileIfPossible(File file)
    {
        try
        {
            File fileCan = file.getCanonicalFile();

            if (fileCan != null)
            {
                file = fileCan;
            }
        }
        catch (IOException e)
        {
        }

        return file;
    }

    public static String getJoinedTrailingPathElements(File file, File rootPath, int maxStringLength, String separator)
    {
        String path = "";

        if (maxStringLength <= 0)
        {
            return "...";
        }

        while (file != null)
        {
            String name = file.getName();

            if (path.isEmpty() == false)
            {
                path = name + separator + path;
            }
            else
            {
                path = name;
            }

            int len = path.length();

            if (len > maxStringLength)
            {
                path = "... " + path.substring(len - maxStringLength, len);
                break;
            }

            if (file.equals(rootPath))
            {
                break;
            }

            file = file.getParentFile();
        }

        return path;
    }

    public static String getNameWithoutExtension(String name)
    {
        int i = name.lastIndexOf(".");
        return i != -1 ? name.substring(0, i) : name;
    }

    public static String generateSimpleSafeFileName(String name)
    {
        return name.toLowerCase(Locale.US).replaceAll("\\W", "_");
    }

    public static String generateSafeFileName(String name)
    {
        StringBuilder sb = new StringBuilder(name.length());

        for (int i = 0; i < name.length(); ++i)
        {
            char c = name.charAt(i);

            if (ILLEGAL_CHARACTERS.contains(c) == false)
            {
                sb.append(c);
            }
        }

        // Some weird reserved windows keywords apparently... FFS >_>
        return sb.toString().replaceAll("COM", "").replaceAll("PRN", "");
    }

    @Nullable
    public static NbtCompound readNBTFile(File file)
    {
        if (file.exists() && file.isFile() && file.canRead())
        {
            try
            {
                FileInputStream is = new FileInputStream(file);
                NbtCompound nbt = NbtIo.readCompressed(is, NbtSizeTracker.ofUnlimitedBytes());
                is.close();
                return nbt;
            }
            catch (Exception e)
            {
                MaLiLib.logger.warn("Failed to read NBT data from file '{}'", file.getAbsolutePath());
            }
        }

        return null;
    }
    public static void setConfigDirectory(File dir)
    {
        if (dir == null)
        {
            MaLiLib.logger.fatal("setConfigDirectory: dir given is NULL.");
        }
        else
        {
            if (dir.isDirectory())
            {
                configDirectory = dir;
            }
            else
            {
                if (dir.mkdir())
                {
                    MaLiLib.logger.info("setConfigDirectory: dir given has been created.");
                }
                else
                {
                    MaLiLib.logger.fatal("setConfigDirectory: dir given failed to be created.");
                }
            }
        }
    }
    public static void setRunDirectory(File dir)
    {
        if (dir == null)
        {
            MaLiLib.logger.fatal("setRunDirectory: dir given is NULL.");
        }
        else
        {
            if (dir.isDirectory())
            {
                runDirectory = dir;
            }
            else
            {
                if (dir.mkdir())
                {
                    MaLiLib.logger.info("setRunDirectory: dir given has been created.");
                }
                else
                {
                    MaLiLib.logger.fatal("setRunDirectory: dir given failed to be created.");
                }
            }
        }
    }
}
