/*
* Code adapted from https://www.baeldung.com/apache-pulsar
*/

package com.klungerbo.streams.pulsar;

import com.klungerbo.streams.pulsar.utils.FileUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Worker that contains the consumer, receiving data from a broker.
 *
 * @version 1.0
 * @since 1.0
 */
public class ConsumerWorker implements Runnable {
    private Consumer<String> consumer = null;
    private boolean running = false;
    private final Logger logger = LoggerFactory.getLogger(ConsumerWorker.class);
    private static final String CONFIG_NAME = "consumerconfig.properties";
    private Set<String> topics = new HashSet<>();

    /**
     * Initializes and prepares the consumer for use.
     */
    public void initialize() {
        try {
            createConsumer();
        } catch (IOException ioe) {
            logger.error("Exception occurred during construction of consumer");
            ioe.printStackTrace();
        }
        this.start();
    }

    /**
     * Starts the thread for the consumer, also starting the consumer itself.
     */
    public void start() {
        this.initialize();
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Attempts to gracefully stop the consumer.
     */
    public void stop() {
        running = false;
        try {
            consumer.close();
        } catch (PulsarClientException pce) {
            logger.error("An error occurred while closing the Pulsar consumer");
        }
    }

    /**
     * Creates the consumer object, and sets required configuration before consumption.
     *
     * @throws IOException if there is a problem getting the config file for the worker
     */
    private void createConsumer() throws IOException {
        Properties props = FileUtils.loadConfigFromFile(CONFIG_NAME);
        Map<String, Object> consumerConfigurations = getConsumerPropertiesAsMap(props);
        String host = System.getenv().getOrDefault("PULSAR_BROKER_URL",
            props.getProperty("pulsar.url", "pulsar://localhost:6650"));

        PulsarClient client = PulsarClient.builder().serviceUrl(host).build();
        consumer = client.newConsumer(Schema.STRING)
            .loadConf(consumerConfigurations)
            .subscribe();
    }

    /**
     * Gets a mapped version of supplied properties for the pulsar consumer. Adds additional
     * configurations as necessary.
     *
     * @param props properties to carry over into the returned map
     * @return map of all relevant configurations in String-Object pairs
     */
    private Map<String, Object> getConsumerPropertiesAsMap(Properties props) {
        Map<String, Object> consumerProperties = new HashMap<>();

        String topic = System.getenv().getOrDefault("TOPIC_NAME",
            props.getProperty("pulsar.topic", "Testtopic"));
        topics.add(topic);

        String subscriptionName = System.getenv().getOrDefault("SUBSCRIPTION_NAME",
            props.getProperty("pulsar.subscriptionName", "subscription"));

        consumerProperties.put("topicNames", topics);
        consumerProperties.put("subscriptionName", subscriptionName);

        return consumerProperties;
    }

    /**
     * Continuously receives messages from the broker, and displays the messages in terminal
     * as they are received.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Message<String> message = consumer.receive();
                consumer.acknowledge(message);
                logger.debug("Consumer received message {}", message);
            } catch (PulsarClientException e) {
                e.printStackTrace();
            }
        }
    }
}
