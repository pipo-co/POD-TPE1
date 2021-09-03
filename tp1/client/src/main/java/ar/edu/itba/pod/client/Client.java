package ar.edu.itba.pod.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tp1 Client Starting ...");
        while(true) {
            Thread.sleep(1000);
            logger.info("Client info");
        }
    }
}
