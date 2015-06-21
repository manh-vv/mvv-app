package mvv.app.process;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mvv.app.MainClass;
import mvv.app.entity.DictionaryEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Manh Vu
 */
public class MyChunkProcess {
    private static final Logger log = LogManager.getLogger(MyChunkProcess.class);

    private static ExecutorService executor;

    private int chunkCount;

    /**
     * @param n
     *            default = 2
     */
    public MyChunkProcess(int n) {
        getExecutor(n > 0 ? n : 2);
    }

    public static ExecutorService getExecutor(int n) {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(n);
        }

        return executor;
    }

    public int process(MainClass waiter, List<DictionaryEntity> entityContainer) {
        if (entityContainer != null) {
            executor.execute(new InsertToDbTask(waiter, ++chunkCount, entityContainer));
            return 1;
        }

        return 0;
    }

    public void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            executor.shutdown();
        }

    }
}
