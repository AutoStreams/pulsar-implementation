package com.autostreams.pulsar;

import static com.autostreams.pulsar.PulsarProducerLoader.createProducer;

/**
 * The class containing the main entry point of the Pulsar producer prototype application.
 *
 * @version 0.1
 * @since 0.1
 */
public final class Main {
    /**
     * The entrypoint of the application.
     *
     * @param args commandline arguments.
     */
    public static void main(final String[] args) {
        PulsarProducer pulsarProducer = createProducer();

        DataReceiver dataReceiver = new DataReceiver(pulsarProducer, port);
        dataReceiver.run();
    }
}