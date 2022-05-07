package com.autostreams.pulsar.receiver;

import static com.autostreams.utils.fileutils.FileUtils.loadPropertiesFromFile;

import com.autostreams.utils.datareceiver.DataReceiver;
import com.autostreams.utils.datareceiver.StreamsServer;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that can create a Data receiver from configuration variables.
 */
public class DataReceiverCreator {
    private static int port;
    private static final Logger logger = LoggerFactory.getLogger(DataReceiverCreator.class);
    private static final String LISTEN_PORT = "listen.port";
    private static final String CONFIG_PROPERTIES = "config.properties";

    private DataReceiverCreator() {}

    /**
     * Creates a new DataReceiver from environment or properties configuration.
     * Environment variables are prioritized over properties variables.
     *
     * @param streamsServer the streams' server which the data-receiver should send messages to.
     * @return a newly created data receiver.
     */
    public static DataReceiver createReceiver(StreamsServer<String> streamsServer) {
        loadConfigurationVariables();
        printConfigurationVariables();

        return new DataReceiver(streamsServer, port);
    }

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
     * @return true if it is possible to load configuration from environment, false if else.
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

}
