package com.autostreams.pulsar.dataprovider;

/**
 * The class containing the main entry point of the data producer application.
 *
 * @version 1.0
 * @since 1.0
 */
public class Main {

    /**
     * The main entry point of the application.
     *
     * @param args the commandline arguments.
     */
    public static void main(String[] args) {
        DataProvider dataProvider = new DataProvider();
        if (dataProvider.initialize()) {
            dataProvider.setMessagesPerSecond(1);
            dataProvider.run();
        }
    }
}