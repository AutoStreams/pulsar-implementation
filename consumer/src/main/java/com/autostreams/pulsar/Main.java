package com.autostreams.pulsar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing main entry point of the consumer application.
 *
 * @version 1.0
 * @since 0.1
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Main function, entry point for consumer program.
     *
     * @param args optional arguments
     */
    public static void main(String[] args) {
        int consumerCount = getConsumerCount(args);

        ConsumerMaster consumerMaster = new ConsumerMaster();
        consumerMaster.init(consumerCount);
        consumerMaster.startWorkers();
    }

    /**
     * Gets the appropriate consumer count for the ConsumerMaster based on properties or input.
     *
     * @param args commandline arguments
     * @return count of consumers
     */
    private static int getConsumerCount(String[] args) {
        int count = 0;
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        options.addOption("w", true, "amount of workers");
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption('w')) {
                count = Integer.parseInt(cmd.getOptionValue('w'));
            }
        } catch (ParseException pe) {
            logger.error("Could not parse commandline arguments");
            pe.printStackTrace();
        } catch (NumberFormatException ne) {
            logger.error("Provided worker argument is not a number");
            ne.printStackTrace();
        }
        return count;
    }
}