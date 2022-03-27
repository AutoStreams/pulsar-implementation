/**
 * Code adapted from:
 * https://pulsar.apache.org/docs/en/client-libraries-java/
 */

package com.klungerbo.streams.pulsar;

import com.klungerbo.streams.utils.datareceiver.StreamsServer;
import com.klungerbo.streams.utils.fileutils.FileUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing a Consumer Master, responsible for creating and controlling Consumer Workers.
 *
 * @version 1.0
 * @since 1.0
 */
public class ConsumerMaster implements StreamsServer<String> {
    private static final String CONFIG_NAME = "masterconfig.properties";
    private final ArrayList<ConsumerWorker> workers = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(ConsumerMaster.class);

    /**
     * Initializes the Consumer Master, starts generation of workers.
     *
     * @param consumerCount amount of consumers to generate
     */
    public void init(int consumerCount) {
        this.generateWorkers(consumerCount);
    }

    /**
     * Generates workers belonging to the master.
     *
     * @param consumerCount amount of workers to create
     */
    private void generateWorkers(int consumerCount) {
        if (consumerCount == 0) {
            try {
                logger.info("Attempting to load properties from file");
                Properties props = FileUtils.loadConfigFromFile(CONFIG_NAME);
                consumerCount = Integer.parseInt(props.getProperty("consumers.count"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("Consumer generation started. {} workers ordered", consumerCount);
        for (int i = 0; i < consumerCount; i++) {
            ConsumerWorker cw = new ConsumerWorker();
            cw.initialize();
            workers.add(cw);
        }
    }

    /**
     * Starts the workers of the master.
     */
    public void startWorkers() {
        for (ConsumerWorker worker : workers) {
            worker.start();
        }
    }

    /**
     * Method handling received messages.
     *
     * @param s message received
     */
    @Override
    public void onMessage(String s) {
        logger.info(s);
    }

    /**
     * Shuts down all workers associated with this master.
     */
    @Override
    public void onShutdown() {
        for (ConsumerWorker worker : workers) {
            worker.stop();
        }
    }
}
