package net.gegy1000.earth.server.world.gen;

import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.util.TempFileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DataMap {
    private final int WIDTH;
    private final int HEIGHT;
    private final FileChannel FILE_CHANNEL;
    private final boolean SIGNED;
    private final int VERSION;

    private final Map<Integer, Byte> DATA_CACHE = new HashMap<>();

    private DataMap(int width, int height, FileChannel fileChannel, boolean signed, int version) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.FILE_CHANNEL = fileChannel;
        this.SIGNED = signed;
        this.VERSION = version;
    }

    public static DataMap construct(String resource, boolean signed, int version) throws IOException {
        InputStream in = DataMap.class.getResourceAsStream(resource);
        String[] resourceFiles = resource.split("/");
        String fileName = resourceFiles[resourceFiles.length - 1];

        int tempVersion = -1;
        File versionFile = TempFileUtil.getTempFile(fileName.split(Pattern.quote("."))[0] + ".version");
        if (versionFile.exists()) {
            BufferedReader versionIn = new BufferedReader(new FileReader(versionFile));
            try {
                tempVersion = Integer.parseInt(versionIn.readLine());
            } catch (Exception e) {
            }
        }

        File tempFile = TempFileUtil.getTempFile(fileName);

        if (tempVersion != version) {
            tempFile = TempFileUtil.createTempFile(fileName);
            copyFile(in, tempFile);
            Earth.LOGGER.info(fileName + " was outdated. Updating.");
            if (!versionFile.exists()) {
                versionFile.createNewFile();
            }
            PrintWriter out = new PrintWriter(new FileWriter(versionFile));
            out.print(version);
            out.close();
        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r");
        FileChannel channel = randomAccessFile.getChannel();

        int width = readInteger(0, channel);
        int height = readInteger(4, channel);

        return new DataMap(width, height, channel, signed, version);
    }

    public int getData(int x, int y) {
        try {
            int position = ((this.WIDTH * y) + x) + 8;

            int data;

            if (!this.DATA_CACHE.containsKey(position)) {
                data = (int) readByte(position, this.FILE_CHANNEL);
                this.DATA_CACHE.put(position, (byte) data);
            } else {
                data = this.DATA_CACHE.get(position);
            }

            if (!this.SIGNED) {
                data += 128;
            }

            return Math.min(data, 255);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getWidth() {
        return this.WIDTH;
    }

    public int getHeight() {
        return this.HEIGHT;
    }

    public void clearCache() {
        this.DATA_CACHE.clear();
    }

    private static byte readByte(int position, FileChannel channel) throws IOException {
        channel.position(position);
        ByteBuffer buf = ByteBuffer.allocate(1);
        channel.read(buf);

        return buf.get(0);
    }

    private static int readInteger(int position, FileChannel channel) throws IOException {
        channel.position(position);

        ByteBuffer buf = ByteBuffer.allocate(4);
        channel.read(buf);

        return ByteBuffer.wrap(buf.array()).getInt();
    }

    private static void copyFile(InputStream in, File to) throws IOException {
        if (!to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }

        if (!to.exists()) {
            to.createNewFile();
        }

        FileOutputStream out = new FileOutputStream(to);

        byte[] buffer = new byte[8024];
        int n;

        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }

        in.close();
        out.close();
    }
}
