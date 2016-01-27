package net.gegy1000.earth.server.world.gen;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DataMap
{
    private int width, height;
    private FileChannel fileChannel;
    private boolean byteOffset;

    private Map<Integer, Byte> dataCache = new HashMap<Integer, Byte>();

    private DataMap(int width, int height, FileChannel fileChannel, boolean byteOffset)
    {
        this.width = width;
        this.height = height;
        this.fileChannel = fileChannel;
        this.byteOffset = byteOffset;
    }

    public static DataMap construct(String resource, boolean byteOffset) throws IOException
    {
        InputStream in = DataMap.class.getResourceAsStream(resource);
        String[] resourceFiles = resource.split("/");
        String fileNameWithExtention = resourceFiles[resourceFiles.length - 1];
        String[] fileNameExtensionSplit = fileNameWithExtention.split(Pattern.quote("."));

        File heightmapFile = createTempFile(fileNameExtensionSplit[0], fileNameExtensionSplit[1]);
        copyFile(in, heightmapFile);

        RandomAccessFile randomAccessFile = new RandomAccessFile(heightmapFile, "r");
        FileChannel channel = randomAccessFile.getChannel();

        int width = readInteger(0, channel);
        int height = readInteger(4, channel);

        return new DataMap(width, height, channel, byteOffset);
    }

    public int getData(int x, int y)
    {
        try
        {
            int position = ((width * y) + x) + 8;

            int data;

            if (!dataCache.containsKey(position))
            {
                data = (int) readByte(position, fileChannel);

                dataCache.put(position, (byte) data);
            }
            else
            {
                data = dataCache.get(position);
            }

            if (byteOffset)
            {
                data += 128;
            }

            return Math.min(data, 255);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void clearCache()
    {
        dataCache.clear();
    }

    private static byte readByte(int position, FileChannel channel) throws IOException
    {
        channel.position(position);
        ByteBuffer buf = ByteBuffer.allocate(1);
        channel.read(buf);

        return buf.get(0);
    }

    private static int readInteger(int position, FileChannel channel) throws IOException
    {
        channel.position(position);

        ByteBuffer buf = ByteBuffer.allocate(4);
        channel.read(buf);

        byte b0 = buf.get(0);
        byte b1 = buf.get(1);
        byte b2 = buf.get(2);
        byte b3 = buf.get(3);

        return ByteBuffer.wrap(new byte[] { b0, b1, b2, b3 }).getInt();
    }

    private static void copyFile(InputStream in, File to) throws IOException
    {
        if (!to.getParentFile().exists())
        {
            to.getParentFile().mkdirs();
        }

        if (!to.exists())
        {
            to.createNewFile();
        }

        FileOutputStream out = new FileOutputStream(to);

        byte[] buffer = new byte[8024];
        int n;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
        }

        in.close();
        out.close();
    }

    private static File createTempFile(String suffix, String format) throws IOException
    {
        String tempDir = System.getProperty("java.io.tmpdir");

        final File temp = new File(tempDir, "tempearthmod" + suffix + "." + format);

        if (!(temp.exists()) && !(temp.createNewFile()))
        {
            throw new IOException("Could not create temp file: " + temp.getAbsolutePath());
        }

        return temp;
    }
}
