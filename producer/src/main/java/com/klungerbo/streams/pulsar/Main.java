package com.klungerbo.streams.pulsar;

import com.klungerbo.streams.utils.datareceiver.DataReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class containing the main entry point of the Pulsar producer prototype application.
 *
 * @version 1.0
 * @since 1.0
 */
public final class Main {
    /**
     * The entrypoint of the application.
     *
     * @param args commandline arguments.
     */
    public static void main(final String[] args) throws InterruptedException {
        final Logger logger = LoggerFactory.getLogger(Main.class);

        logger.info("Waiting here");
        Thread.sleep(30000);

        PulsarPrototypeProducer pulsarPrototypeProducer = new PulsarPrototypeProducer();
        if (!pulsarPrototypeProducer.initialize()) {
            logger.error("Failed to initialize PulsarPrototypeProducer");
            return;
        }

        logger.debug("Creating DataReceiver for PulsarProducer");
        DataReceiver dataReceiver = new DataReceiver(pulsarPrototypeProducer);
        logger.debug("DataReceiver for PulsarProducer has been created");

        logger.debug("DataReceiver running");
        dataReceiver.run();
        logger.debug("DataReceiver ran for PulsarProducer, has finished");
    }
}