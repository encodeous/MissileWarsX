package ca.encodeous.mwx.structure;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;

import java.io.File;
import java.io.FileInputStream;

/**
 * Represents a loaded structure (missile, shield, etc.): schematic clipboard data read into
 * memory via FAWE.
 * Red team uses the clipboard as-is (blocks extend in the +Z direction from the placement
 * origin). Green team mirrors on the Z axis at placement time (see {@link StructurePlacer}),
 * so no separate clipboard or pre-baked depot is required.
 */
public class StructureTemplate {

    public final String id;
    public final Clipboard clipboard;

    public final int width;
    public final int height;
    public final int depth;

    private StructureTemplate(String id, Clipboard clipboard, int w, int h, int d) {
        this.id = id;
        this.clipboard = clipboard;
        this.width = w;
        this.height = h;
        this.depth = d;
    }

    /**
     * Load a StructureTemplate from a .schem file using FAWE.
     */
    public static StructureTemplate load(File schemFile, String id) throws Exception {
        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
        if (format == null) {
            throw new IllegalArgumentException("Unknown schematic format: " + schemFile.getName());
        }

        Clipboard clipboard;
        try (FileInputStream fis = new FileInputStream(schemFile);
             ClipboardReader reader = format.getReader(fis)) {
            clipboard = reader.read();
        }

        BlockVector3 dims = clipboard.getDimensions();
        return new StructureTemplate(id, clipboard, dims.x(), dims.y(), dims.z());
    }
}
