package mvv.app.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import mvv.app.exception.HandleError;

/**
 * @author Manh Vu
 */
public enum StringCompresser
{

    ;

    public static byte[] compress(String text) {

        byte[] bytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            OutputStream out = new DeflaterOutputStream(baos);

            out.write(text.getBytes("UTF-8"));

            bytes = baos.toByteArray();

            out.close();
            baos.close();
        } catch (IOException e) {
            throw new HandleError(e);
        }

        return bytes;

    }

    public static String decompress(byte[] bytes) {

        String rs = null;
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            byte[] buffer = new byte[16 * 1024];

            int len;

            while ((len = in.read(buffer)) > 0)
                baos.write(buffer, 0, len);

            rs = new String(baos.toByteArray(), "UTF-8");

            baos.close();
            in.close();
        } catch (IOException e) {
            throw new HandleError(e);
        }

        return rs;
    }

}
