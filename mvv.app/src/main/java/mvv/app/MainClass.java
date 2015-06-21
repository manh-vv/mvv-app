package mvv.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mvv.app.entity.DictionaryEntity;
import mvv.app.exception.HandleError;
import mvv.app.process.AbsTask;
import mvv.app.process.MyChunkProcess;
import mvv.app.sqlite.SqliteHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainClass {
    private static final Logger log = LogManager.getLogger(MainClass.class);

    private SqliteHelper sqliteHelper;
    private MyChunkProcess chunkProcess;
    public boolean isWait;

    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        log.info("App start at {}, current directory: {}", t1, System.getProperty("user.dir"));

        MainClass mainClass = new MainClass();
        try {
            mainClass.onStart();
            mainClass.run();
            mainClass.onFinish();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        long t2 = System.currentTimeMillis();
        log.info("App end at {}; executed time: {}", t2, t2 - t1);
    }

    /**
     *
     * @author Manh Vu
     */
    private void onStart() {
        sqliteHelper = new SqliteHelper("src/main/resources/mvvapp.db3");

        try {
            sqliteHelper.deleteAll(DictionaryEntity.class);
            sqliteHelper.resetIndex(DictionaryEntity.class);
        } catch (SQLException e) {
            throw new HandleError("Cannot reset table", e);
        }

        AbsTask.sqliteHelper = sqliteHelper;
        chunkProcess = new MyChunkProcess(5);
    }

    /**
     *
     * @author Manh Vu
     */
    private void run() {
        File fi = new File("src/main/resources/cam-.txt");
        FileReader fr = null;
        try {
            fr = new FileReader(fi);
            BufferedReader br = new BufferedReader(fr, 16 * 1024);

            final int PROCESS_SIZE = 100000;
            final int CHUNK_SIZE = 6;
            /* process chunk by chunk */
            List<DictionaryEntity> entityContainer = null;
            String line;
            int count = 0;
            long t1 = 0;
            while ((line = br.readLine()) != null) {
                if (entityContainer == null) {
                    t1 = System.currentTimeMillis();
                    entityContainer = new ArrayList<>(PROCESS_SIZE);
                }

                int idx = line.indexOf(" = ");
                if (idx > 0) {
                    DictionaryEntity entity = new DictionaryEntity();
                    entity.word = line.substring(0, idx);
                    entity.definition = line.substring(idx + 3);

                    entityContainer.add(entity);
                } else {
                    log.warn("[IGNORE] line:\n{}", line);
                }

                if (entityContainer.size() == PROCESS_SIZE) {
                    /* spawn thread to process this chunk */
                    log.debug("Read {} lines takes {} ms", entityContainer.size(), System.currentTimeMillis() - t1);
                    count += chunkProcess.process(this, entityContainer);

                    entityContainer = null;

                    if (count == CHUNK_SIZE) {
                        log.trace("chunk size rich {} -> wait untils previous thread finish", count);

                        synchronized (this) {
                            this.isWait = true;
                            this.wait();
                        }

                        count--; // reset count
                        log.trace("continue reading job");
                    }
                }
            }

            br.close();

            chunkProcess.process(null, entityContainer);
            log.debug("Read {} lines takes {} ms", entityContainer.size(), System.currentTimeMillis() - t1);

        } catch (IOException | InterruptedException e) {
            log.error("File path: {}", fi.getAbsoluteFile(), e);
        } finally {
            try {
                if (fr != null) fr.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     *
     * @author Manh Vu
     */
    private void onFinish() {
        chunkProcess.shutdown();
        sqliteHelper.close();
    }
}
