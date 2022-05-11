/*
 * Code adapted from:
 * https://pulsar.apache.org/docs/en/client-libraries-java/
 * https://www.baeldung.com/apache-pulsar
 */

package com.autostreams.pulsar.producer;

import static com.autostreams.utils.fileutils.FileUtils.loadPropertiesFromFile;

import com.autostreams.utils.datareceiver.StreamsServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pulsar producer implementation.
 *
 * @version 1.0
 * @since 0.1
 */
public class PulsarProducer implements StreamsServer<String> {
    private static final String CONFIG_PROPERTIES = "config.properties";
    private static final String PRODUCER_PROPERTIES = "producer.properties";
    private final Logger logger = LoggerFactory.getLogger(PulsarProducer.class);
    private PulsarClient pulsarClient;
    private Producer<String> producer;

    /**
     * Initialize the Pulsar producer.
     */
    public void initialize() {
        String host = loadAndGetHostPropertyVariable();
        Map<String, Object> producerProperties = loadAndGetProducerPropertyVariables();

        connectToBroker(host, producerProperties);
    }

    /**
     * Load the host property variable from environment or the property configuration file.
     * Note: The environment variable is prioritized over the property configuration file variable.
     *
     * @return a string representing the host on the form "pulsar://[IP]:[PORT].
     */
    private static String loadAndGetHostPropertyVariable() {
        Properties configProperties = loadPropertiesFromFile(CONFIG_PROPERTIES);

        return System.getenv().getOrDefault("PULSAR_BROKER_URL",
            configProperties.getProperty("pulsar.broker.url", "pulsar://127.0.0.1:6650")
        );
    }

    /**
     * Load the producer properties from environment or the property configuration file.
     * Note: The environment variables are prioritized over the configuration file variables.
     *
     * @return a map of property keys to property values.
     */
    private static Map<String, Object> loadAndGetProducerPropertyVariables() {
        Properties producerProperties = loadPropertiesFromFile(PRODUCER_PROPERTIES);
        Map<String, String> producerPropertiesMap =
            PulsarProducer.convertPropertiesToMap(producerProperties);

        return PulsarProducer.transformProducerPropertiesMap(producerPropertiesMap);
    }

    /**
     * Tries to connect to the Pulsar broker.
     * The connection is retried every 5 seconds on failure for an unlimited amount of tries.
     */
    private void connectToBroker(String host, Map<String, Object> producerProperties) {
        while (!establishConnection(host, producerProperties)) {
            int secondsToSleep = 5;
            logger.warn(
                "Failed to initialize PulsarProducer, retrying in {} seconds",
                secondsToSleep
            );

            sleepForSeconds(secondsToSleep);
        }
    }

    /**
     * Tries to establish a connection to the Pulsar broker.
     *
     * @param host the ip and port of the Pulsar broker in the form "pulsar://[IP]:[PORT].
     *             Example: "pulsar://127.0.0.1:6650"
     * @param properties a map of property names to properties.
     */
    private boolean establishConnection(String host, Map<String, Object> properties) {
        logger.info("Establishing connection to {}", host);

        try {
            this.pulsarClient = PulsarClient.builder()
                .serviceUrl(host)
                .build();

            this.producer = this.pulsarClient.newProducer(Schema.STRING)
                .loadConf(properties)
                .create();
        } catch (PulsarClientException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Sleep for a specified amount of time in seconds.
     *
     * @param seconds the seconds to sleep for.
     */
    private void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            logger.error("Unable to sleep");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Transforms the producer properties map of String, String to String, Object.
     *
     * @param producerPropertiesMap the producer properties map to sanitize.
     * @return a transformed producer properties map.
     */
    private static HashMap<String, Object> transformProducerPropertiesMap(
        Map<String, String> producerPropertiesMap) {
        HashMap<String, Object> transformedMap = new HashMap<>();

        transformedMap.put(
            "topicName",
            System.getenv().getOrDefault(
                "TOPIC_NAME",
                producerPropertiesMap.get("topicName")
            )
        );

        transformedMap.put(
            "producerName",
            System.getenv().getOrDefault(
                "PRODUCER_NAME",
                producerPropertiesMap.get("producerName")
            )
        );

        transformedMap.put(
            "sendTimeoutMs",
            Long.parseLong(System.getenv().getOrDefault(
                "SEND_TIMEOUT_MS",
                producerPropertiesMap.get("sendTimeoutMs")
            ))
        );

        transformedMap.put(
            "blockIfQueueFull",
            Boolean.parseBoolean(System.getenv().getOrDefault(
                "BLOCK_IF_QUEUE_FULL",
                producerPropertiesMap.get("blockIfQueueFull")
            ))
        );

        transformedMap.put(
            "maxPendingMessages",
            Integer.parseInt(System.getenv().getOrDefault(
                "MAX_PENDING_MESSAGES",
                producerPropertiesMap.get("maxPendingMessages")
            ))
        );

        transformedMap.put(
            "maxPendingMessagesAcrossPartitions",
            Integer.parseInt(System.getenv().getOrDefault(
                "MAX_PENDING_MESSAGES_ACROSS_PARTITIONS",
                producerPropertiesMap.get("maxPendingMessagesAcrossPartitions")
            ))
        );

        transformedMap.put(
            "messageRoutingMode",
            MessageRoutingMode.valueOf(System.getenv().getOrDefault(
                "MESSAGE_ROUTING_MODE",
                producerPropertiesMap.get("messageRoutingMode")
            ))
        );

        transformedMap.put(
            "hashingScheme",
            HashingScheme.valueOf(System.getenv().getOrDefault(
                "HASHING_SCHEME",
                producerPropertiesMap.get("hashingScheme")
            ))
        );

        transformedMap.put(
            "cryptoFailureAction",
            ProducerCryptoFailureAction.valueOf(System.getenv().getOrDefault(
                "CRYPTO_FAILURE_ACTION",
                producerPropertiesMap.get("cryptoFailureAction")
            ))
        );

        transformedMap.put(
            "compressionType",
            CompressionType.valueOf(System.getenv().getOrDefault(
                "COMPRESSION_TYPE",
                producerPropertiesMap.get("compressionType")
            ))
        );

        return transformedMap;
    }

    /**
     * Code adapted from:
     * https://www.baeldung.com/java-convert-properties-to-hashmap
     *
     * @param properties the properties
     * @return a has map containing the properties
     */
    private static HashMap<String, String> convertPropertiesToMap(Properties properties) {
        return properties.entrySet().stream().collect(
            Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue()),
                (prev, next) -> next, HashMap::new
            ));
    }

    /**
     * Send a message to a Pulsar broker through a record.
     *
     * @param message the message to send to the Pulsar broker.
     */
    @Override
    public void onMessage(String message) {
        this.producer.sendAsync(message)
            .thenAcceptAsync(msgId -> logger.debug("{} sent to broker", msgId));
    }

    /**
     * Shutdown the Pulsar producer.
     */
    @Override
    public void onShutdown() {
        logger.info("Attempting to shut down the Pulsar producer");

        if (this.producer != null) {
            try {
                this.producer.close();
                this.pulsarClient.close();
            } catch (PulsarClientException e) {
                e.printStackTrace();
            }
        }
    }
}
