package mvv.app.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Manh Vu
 */
public class StringUtils {
    private static final Logger log = LogManager.getLogger(StringUtils.class);

    /**
     * @author Manh Vu
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.length() < 1);
    }

}
