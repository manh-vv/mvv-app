package mvv.app.utils;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import mvv.app.exception.HandleError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Manh Vu
 */
public interface StringCompressor {
    Logger log = LogManager.getLogger(StringCompressor.class);

    public static byte[] compress(String text) {
        byte[] rs = null;

        try {
            rs = compress(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new HandleError(e);
        }

        return rs;
    }

    public static byte[] compress(byte[] in) {

        byte[] output = new byte[16 * 1024];

        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);

        compresser.setInput(in);
        compresser.finish();

        int compressedDataLength = compresser.deflate(output);
        byte[] dest = new byte[compressedDataLength];

        System.arraycopy(output, 0, dest, 0, compressedDataLength);

        return dest;
    }

    public static String decompress(byte[] bytes) {
        byte[] rs = decompressB(bytes);
        String outputString;

        try {
            // Decode the bytes into a String
            outputString = new String(rs, 0, rs.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new HandleError(e);
        }

        return outputString;
    }

    public static byte[] decompressB(byte[] bytes) {

        Inflater decompresser = new Inflater();

        decompresser.setInput(bytes, 0, bytes.length);

        byte[] result = new byte[bytes.length * 10];
        byte[] dest;

        try {
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            dest = new byte[resultLength];
            System.arraycopy(result, 0, dest, 0, resultLength);

        } catch (DataFormatException e) {
            throw new HandleError(e);
        }

        return dest;
    }

    public static byte[] compressN(byte[] in, int n) {
        byte[] bs = compress(in);
        log.debug("compress times: " + n);

        if (n > 0 && bs != null) compressN(bs, n - 1);

        return bs;
    }

    public static byte[] compressN(String text, int n) {
        byte[] in = null;

        try {
            in = text.getBytes("UTF-8");
            in = compressN(in, n);
        } catch (Exception e) {
            throw new HandleError(e);
        }

        return in;
    }

    public static String decompressN(byte[] in, int n) {

        if (n < 1)
            return decompress(in);

        byte[] bs = decompressB(in);
        log.debug("decompress times: " + n);

        if (n > 1 && bs != null) decompressN(bs, n - 1);

        return decompress(bs);
    }
}
