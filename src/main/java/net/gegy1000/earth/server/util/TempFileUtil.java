package net.gegy1000.earth.server.util;

import java.io.File;
import java.io.IOException;

public class TempFileUtil {
    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "earth_mod");

    static {
        TEMP_DIR.mkdir();
    }

    public static File createTempFile(String name) throws IOException {
        final File temp = new File(TEMP_DIR, name);
        if (!temp.getParentFile().exists()) {
            temp.getParentFile().mkdirs();
        }
        if (!(temp.exists()) && !(temp.createNewFile())) {
            throw new IOException("Could not create temp file: " + temp.getAbsolutePath());
        }
        return temp;
    }

    public static File getTempFile(String name) {
        return new File(TEMP_DIR, name);
    }
}
