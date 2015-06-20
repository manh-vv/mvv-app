package mvv.app;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainClass {
	private static final Logger log = LogManager.getLogger(MainClass.class);

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
        // TODO Auto-generated method stub

    }

    /**
     *
     * @author Manh Vu
     */
    private void run() {
        File fi = new File("cam-.txt");

    }

    /**
     *
     * @author Manh Vu
     */
    private void onFinish() {
        // TODO Auto-generated method stub

    }
}
