package mvv.app.utils;

import java.util.Collection;

/**
 * @author Manh Vu
 */
public class CollectionUtils {
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.size() < 1);
    }
}
