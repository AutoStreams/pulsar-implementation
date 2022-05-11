/**
 * Code adapted from https://www.baeldung.com/apache-pulsar
 */

package com.autostreams.pulsar;

import java.io.IOException;
import java.util.Map;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker that contains the consumer, receiving data from a broker.
 *
 * @version 1.0
 * @since 0.1
 */
public class ConsumerWorker implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ConsumerWorker.class);
    private Consumer<String> consumer = null;
    private boolean running = false;
    private final ConsumerPropertyLoader propertyLoader;

    /**
     * Public constructor for the consumer worker class.
     */
    public ConsumerWorker() {
        propertyLoader = new ConsumerPropertyLoader();
    }

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
        running = true;
    }

    /**
     * Starts the thread for the consumer, also starting the consumer itself.
     */
    public void start() {
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
        String host = propertyLoader.getHost();
        Map<String, Object> consumerProperties = propertyLoader.getConsumerConfiguration();

        PulsarClient client = PulsarClient
                .builder()
                .serviceUrl(host)
                .build();

        this.consumer = client.newConsumer(Schema.STRING)
            .loadConf(consumerProperties)
            .subscribe();

        logger.info("Consumer created, topic subscribed to");
    }

    /**
     * Listens for messages from the broker, and acknowledges said messages.
     */
    private void receive() {
        while (running) {
            Message<String> message = null;

            try {
                logger.info("Waiting to receive message...");
                message = this.consumer.receive();

                this.consumer.acknowledge(message);
                logger.info("Consumer received message {}", message.getValue());

            } catch (PulsarClientException e) {
                consumer.negativeAcknowledge(message);
                e.printStackTrace();
            }
        }
    }

    /**
     * Continuously receives messages from the broker, and displays the messages in terminal
     * as they are received.
     */
    @Override
    public void run() {
        receive();
    }

}
