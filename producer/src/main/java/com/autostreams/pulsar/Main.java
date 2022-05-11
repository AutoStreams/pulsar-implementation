package com.autostreams.pulsar;


import com.autostreams.pulsar.producer.PulsarProducer;
import com.autostreams.pulsar.receiver.DataReceiverCreator;
import com.autostreams.utils.datareceiver.DataReceiver;

/**
 * The class containing the main entry point of the Pulsar producer application.
 *
 * @version 1.0
 * @since 0.1
 */
public final class Main {
    /**
     * The entrypoint of the application.
     *
     * @param args commandline arguments.
     */
    public static void main(final String[] args) {
        PulsarProducer pulsarProducer = new PulsarProducer();
        pulsarProducer.initialize();

        DataReceiver dataReceiver = DataReceiverCreator.createReceiver(pulsarProducer);
        dataReceiver.run();
    }
}
