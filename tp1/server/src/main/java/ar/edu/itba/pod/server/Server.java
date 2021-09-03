package ar.edu.itba.pod.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tp1 Server Starting ...");
        while(true) {
            Thread.sleep(1000);
            logger.info("Server info");
        }
    }
}
