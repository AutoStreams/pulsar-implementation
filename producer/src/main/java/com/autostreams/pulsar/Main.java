package com.autostreams.pulsar;

import static com.autostreams.utils.fileutils.FileUtils.loadPropertiesFromFile;

import com.autostreams.datareceiver.DataReceiver;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class containing the main entry point of the Pulsar producer prototype application.
 *
 * @version 0.1
 * @since 0.1
 */
public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String LISTEN_PORT = "listen.port";
    private static final String CONFIG_PROPERTIES = "config.properties";
    static int port;

    /**
     * The entrypoint of the application.
     *
     * @param args commandline arguments.
     */
    public static void main(final String[] args) {
        loadConfigurationVariables();
        printConfigurationVariables();

        PulsarProducer pulsarProducer = createProducer();

        DataReceiver dataReceiver = new DataReceiver(pulsarProducer, port);
        dataReceiver.run();
    }

    private static void printConfigurationVariables() {
        logger.info("Port: {}", port);
    }

    private static void loadConfigurationVariables() {
        if (canLoadFromEnvironment()) {
            loadFromEnvironment();
        } else if (canLoadFromProperties()) {
            loadFromProperties();
        } else {
            setDefault();
        }
    }

    private static void loadFromEnvironment() {
        port = Integer.parseInt(System.getenv().get(LISTEN_PORT));
    }

    private static void setDefault() {
        port = 8992;
    }

    private static boolean canLoadFromEnvironment() {
        return System.getenv().containsKey(LISTEN_PORT);
    }

    private static boolean canLoadFromProperties() {
        Properties properties = loadPropertiesFromFile(CONFIG_PROPERTIES);
        return properties.containsKey(LISTEN_PORT);
    }

    private static void loadFromProperties() {
        Properties properties = loadPropertiesFromFile(CONFIG_PROPERTIES);
        port = Integer.parseInt((String) properties.get(LISTEN_PORT));
    }

    private static PulsarProducer createProducer() {
        PulsarProducer pulsarProducer = new PulsarProducer();
        while (!pulsarProducer.initialize()) {
            int secondsToSleep = 5;
            logger.warn(
                "Failed to initialize PulsarPrototypeProducer, retrying in {} seconds",
                secondsToSleep
            );

            sleepForSeconds(secondsToSleep);
        }

        return  pulsarProducer;
    }

    private static void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            logger.error("Unable to sleep");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}