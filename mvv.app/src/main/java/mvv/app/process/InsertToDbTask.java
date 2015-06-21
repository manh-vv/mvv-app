package mvv.app.process;

import java.sql.SQLException;
import java.util.List;

import mvv.app.MainClass;
import mvv.app.entity.DictionaryEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Manh Vu
 */
public class InsertToDbTask extends AbsTask implements Runnable {
    private static final Logger log = LogManager.getLogger(InsertToDbTask.class);

    private List<DictionaryEntity> entityContainer;
    private MainClass waiter;
    private String id;

    public InsertToDbTask(MainClass waiter, int chunkCount, List<DictionaryEntity> entityContainer) {
        this.waiter = waiter;
        this.entityContainer = entityContainer;
        this.id = "Chunk index: " + chunkCount;
    }

    @Override
    public void run() {
        long t1 = System.currentTimeMillis();
        log.trace("{} start at {}", id, t1);
        try {
            for (DictionaryEntity entity : entityContainer) {
                sqliteHelper.insert(entity);
            }

            sqliteHelper.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            sqliteHelper.rollback();
        }

        if (waiter != null && waiter.isWait) {
            synchronized (waiter) {
                waiter.notify();
                waiter.isWait = false;
            }
        }

        long t2 = System.currentTimeMillis();
        log.trace("{} finishes at {}, execute time {}", id, t2, t2 - t1);
    }

}