package mvv.app.test;

import mvv.app.utils.StringCompressor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Manh Vu
 */
public class TestCompressor {

    private static final Logger log = LogManager.getFormatterLogger(TestCompressor.class);

    public static void main(String[] args) {
        // get String
        String text = MyText.text;
        log.info("text = " + text);
        log.info("lenght = " + text.length());
        log.info("bytes length = " + text.getBytes().length);

        int n = 15;

        byte[] c = StringCompressor.compressN(text, n);
        log.info("c length = " + c.length);

//        text = StringCompressor.decompressN(c, n);
        log.info("text = " + text);
    }
}
