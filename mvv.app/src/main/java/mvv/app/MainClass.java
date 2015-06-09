package mvv.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainClass {
	private static final Logger log = LogManager.getLogger(MainClass.class);
	
	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		log.info("App start at {}", t1);
		
		long t2 = System.currentTimeMillis();
		log.info("App end at {}; executed time: {}", t2, t2 - t1);
	}
}
