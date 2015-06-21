package mvv.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mvv.app.entity.DictionaryEntity;
import mvv.app.process.AbsTask;
import mvv.app.process.MyChunkProcess;
import mvv.app.sqlite.SqliteHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainClass {
    private static final Logger log = LogManager.getLogger(MainClass.class);

    private SqliteHelper sqliteHelper;
    private MyChunkProcess chunkProcess;
    public volatile boolean isWait;

    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        log.info("App start at {}, current directory: {}", t1, System.getProperty("user.dir"));

        MainClass mainClass = new MainClass();
        mainClass.onStart();
        mainClass.run();
        mainClass.onFinish();

        long t2 = System.currentTimeMillis();
        log.info("App end at {}; executed time: {}", t2, t2 - t1);
    }

    /**
     *
     * @author Manh Vu
     */
    private void onStart() {
        sqliteHelper = new SqliteHelper("src/main/resources/mvvapp.db3");
        AbsTask.sqliteHelper = sqliteHelper;
        chunkProcess = new MyChunkProcess(4);
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
            BufferedReader br = new BufferedReader(fr);

            final int PROCESS_SIZE = 100;
            final int CHUNK_SIZE = 8;
            /* process chunk by chunk */
            List<DictionaryEntity> entityContainer = null;
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (entityContainer == null) entityContainer = new ArrayList<>(PROCESS_SIZE);

                int idx = line.indexOf('=');
                if (idx > 0) {
                    DictionaryEntity entity = new DictionaryEntity();
                    entity.word = line.substring(0, idx - 1);
                    entity.definition = line.substring(idx + 2);

                    entityContainer.add(entity);
                }

                if (entityContainer.size() == PROCESS_SIZE) {
                    /* spawn thread to process this chunk */
                    count += chunkProcess.process(this, entityContainer);
                    entityContainer = null;

                    if (count == CHUNK_SIZE) {
                        log.trace("chunk size rich {} -> wait untils previous thread finish", count);
                        synchronized (this) {
                            this.isWait = true;
                            this.wait();
                        }
                        count = 0; // reset count
                        log.trace("continue reading job");
                    }
                }
            }

            chunkProcess.process(null, entityContainer);

            br.close();
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
