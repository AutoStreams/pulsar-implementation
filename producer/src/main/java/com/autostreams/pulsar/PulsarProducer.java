/*
 * Code adapted from:
 * https://pulsar.apache.org/docs/en/client-libraries-java/
 * https://www.baeldung.com/apache-pulsar
 */

package com.autostreams.pulsar;

import static com.autostreams.utils.fileutils.FileUtils.loadPropertiesFromFile;

import com.autostreams.datareceiver.StreamsServer;
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
 * A prototype of a Pulsar producer.
 *
 * @version 0.1
 * @since 0.1
 */
public class PulsarProducer implements StreamsServer<byte[]> {
    private static final String CONFIG_PROPERTIES = "config.properties";
    private static final String PRODUCER_PROPERTIES = "producer.properties";
    private final Logger logger = LoggerFactory.getLogger(PulsarProducer.class);
    PulsarClient pulsarClient;
    Producer<byte[]> producer;

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
     * Sanitizes the producer properties map of String, String to String, Object.
     *
     * @param producerPropertiesMap the producer properties map to sanitize.
     * @return a sanitized producer properties map.
     */
    private static HashMap<String, Object> sanitizeProducerPropertiesMap(
        Map<String, String> producerPropertiesMap) {
        HashMap<String, Object> sanitizedMap = new HashMap<>();

        sanitizedMap.put(
            "topicName",
            System.getenv().getOrDefault(
                "TOPIC_NAME",
                producerPropertiesMap.get("topicName")
            )
        );

        sanitizedMap.put(
            "producerName",
            System.getenv().getOrDefault(
                "PRODUCER_NAME",
                producerPropertiesMap.get("producerName")
            )
        );

        sanitizedMap.put(
            "sendTimeoutMs",
            Long.parseLong(System.getenv().getOrDefault(
                "SEND_TIMEOUT_MS",
                producerPropertiesMap.get("sendTimeoutMs")
            ))
        );

        sanitizedMap.put(
            "blockIfQueueFull",
            Boolean.parseBoolean(System.getenv().getOrDefault(
                "BLOCK_IF_QUEUE_FULL",
                producerPropertiesMap.get("blockIfQueueFull")
            ))
        );

        sanitizedMap.put(
            "maxPendingMessages",
            Integer.parseInt(System.getenv().getOrDefault(
                "MAX_PENDING_MESSAGES",
                producerPropertiesMap.get("maxPendingMessages")
            ))
        );

        sanitizedMap.put(
            "maxPendingMessagesAcrossPartitions",
            Integer.parseInt(System.getenv().getOrDefault(
                "MAX_PENDING_MESSAGES_ACROSS_PARTITIONS",
                producerPropertiesMap.get("maxPendingMessagesAcrossPartitions")
            ))
        );

        sanitizedMap.put(
            "messageRoutingMode",
            MessageRoutingMode.valueOf(System.getenv().getOrDefault(
                "MESSAGE_ROUTING_MODE",
                producerPropertiesMap.get("messageRoutingMode")
            ))
        );

        sanitizedMap.put(
            "hashingScheme",
            HashingScheme.valueOf(System.getenv().getOrDefault(
                "HASHING_SCHEME",
                producerPropertiesMap.get("hashingScheme")
            ))
        );

        sanitizedMap.put(
            "cryptoFailureAction",
            ProducerCryptoFailureAction.valueOf(System.getenv().getOrDefault(
                "CRYPTO_FAILURE_ACTION",
                producerPropertiesMap.get("cryptoFailureAction")
            ))
        );

        sanitizedMap.put("batchingMaxPublishDelayMicros",
            TimeUnit.MILLISECONDS.toMicros(
                Long.parseLong(System.getenv().getOrDefault(
                    "BATCHING_MAX_PUBLISH_DELAY_MICROS",
                    producerPropertiesMap.get("batchingMaxPublishDelayMicros")
                ))
            )
        );

        sanitizedMap.put(
            "batchingMaxMessages",
            Integer.parseInt(System.getenv().getOrDefault(
                "BATCHING_MAX_MESSAGES",
                producerPropertiesMap.get("batchingMaxMessages")
            ))
        );

        sanitizedMap.put(
            "batchingEnabled",
            Boolean.parseBoolean(System.getenv().getOrDefault(
                "BATCHING_ENABLED",
                producerPropertiesMap.get("batchingEnabled")
            ))
        );

        sanitizedMap.put(
            "compressionType",
            CompressionType.valueOf(System.getenv().getOrDefault(
                "COMPRESSION_TYPE",
                producerPropertiesMap.get("compressionType")
            ))
        );

        return sanitizedMap;
    }

    /**
     * Initialize the Pulsar prototype producer.
     *
     * @return true if successful, false if else.
     */
    public boolean initialize() {
        Properties configProperties;
        Properties producerProperties;

        configProperties = loadPropertiesFromFile(CONFIG_PROPERTIES);
        producerProperties = loadPropertiesFromFile(PRODUCER_PROPERTIES);

        final String host = System.getenv().getOrDefault("PULSAR_BROKER_URL",
            configProperties.getProperty("pulsar.broker.url", "pulsar://127.0.0.1:6650")
        );

        final Map<String, String> producerPropertiesMap =
            PulsarProducer.convertPropertiesToMap(producerProperties);
        final Map<String, Object> sanitizedPropertiesMap =
            PulsarProducer.sanitizeProducerPropertiesMap(producerPropertiesMap);

        try {
            logger.info("Establishing connection to {}", host);
            this.pulsarClient = PulsarClient.builder()
                .serviceUrl(host)
                .build();

            this.producer = this.pulsarClient.newProducer(Schema.BYTES)
                .loadConf(sanitizedPropertiesMap)
                .create();

        } catch (PulsarClientException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Send a message to a Pulsar broker through a record.
     *
     * @param message the message to send to the Pulsar broker.
     */
    @Override
    public void onMessage(byte[] message) {
        this.producer.sendAsync(message)
            .thenAcceptAsync(msgId -> logger.debug("{} sent to broker", msgId));
    }

    /**
     * Shutdown the Pulsar prototype producer.
     */
    @Override
    public void onShutdown() {
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