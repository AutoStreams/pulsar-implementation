package com.autostreams.pulsar;

import static com.autostreams.utils.fileutils.FileUtils.loadPropertiesFromFile;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PulsarProducerLoader {
    private static final String LISTEN_PORT = "listen.port";
    private static final String CONFIG_PROPERTIES = "config.properties";
    static int port;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Prints the configuration variables.
     */
    private static void printConfigurationVariables() {
        logger.info("Port: {}", port);
    }

    /**
     * Load configuration for the Pulsar producer.
     */
    private static void loadConfigurationVariables() {
        if (canLoadFromEnvironment()) {
            loadFromEnvironment();
        } else if (canLoadFromProperties()) {
            loadFromProperties();
        } else {
            setDefault();
        }
    }

    /**
     * Load configuration from environment variables.
     */
    private static void loadFromEnvironment() {
        port = Integer.parseInt(System.getenv().get(LISTEN_PORT));
    }

    /**
     * Set configuration from default values.
     */
    private static void setDefault() {
        port = 8992;
    }

    /**
     * Check if it is possible to load configuration from environment variables.
     *
     * @return true if it is possible to load configuration from environment variables, false if else.
     */
    private static boolean canLoadFromEnvironment() {
        return System.getenv().containsKey(LISTEN_PORT);
    }

    /**
     * Check if it is possible to load configuration from properties file.
     *
     * @return true if it is possible to load configuration from properties file, false if else.
     */
    private static boolean canLoadFromProperties() {
        Properties properties = loadPropertiesFromFile(CONFIG_PROPERTIES);
        return properties.containsKey(LISTEN_PORT);
    }

    /**
     * Load configuration from properties file.
     */
    private static void loadFromProperties() {
        Properties properties = loadPropertiesFromFile(CONFIG_PROPERTIES);
        port = Integer.parseInt((String) properties.get(LISTEN_PORT));
    }

    /**
     * Create a new Pulsar producer.
     *
     * @return the newly created Pulsar producer.
     */
    public static PulsarProducer createProducer() {
        loadConfigurationVariables();
        printConfigurationVariables();

        PulsarProducer pulsarProducer = new PulsarProducer();
        while (!pulsarProducer.initialize()) {
            int secondsToSleep = 5;
            logger.warn(
                "Failed to initialize PulsarPrototypeProducer, retrying in {} seconds",
                secondsToSleep
            );

            sleepForSeconds(secondsToSleep);
        }

        return pulsarProducer;
    }

    /**
     * Sleep for a specified amount of time in seconds.
     *
     * @param seconds the seconds to sleep for.
     */
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
