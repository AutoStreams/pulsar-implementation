/**
 * Code adapted from:
 * https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example/securechat
 */

package com.autostreams.pulsar.dataprovider;

import com.autostreams.utils.fileutils.FileUtils;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data producer (client) that connects to a producer (server) in order to send messages.
 *
 * @version 1.0
 * @since 1.0
 */
public final class DataProvider {
    private static final String CONFIG_PROPERTIES_FILE_NAME = "config.properties";
    private static final String HOST_PROPERTIES_VARIABLE_NAME = "producer.url";
    private static final String PORT_PROPERTIES_VARIABLE_NAME = "producer.port";

    private static final String HOST_ENVIRONMENT_VARIABLE_NAME = "PRODUCER_URL";
    private static final String PORT_ENVIRONMENT_VARIABLE_NAME = "PRODUCER_PORT";

    private String host = "127.0.0.1";
    private Integer port = 8992;

    private final Logger logger = LoggerFactory.getLogger(DataProvider.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private final Lorem lorem = LoremIpsum.getInstance();
    private EventLoopGroup group = new NioEventLoopGroup();
    private boolean running = true;
    private ChannelFuture channelFuture = null;
    private int messagesPerSecond = 1;

    /**
     * Default constructors that uses environment or properties variables.
     */
    public DataProvider() {
        setValues();
    }

    /**
     * Set values from environment variables or properties.
     * Note: Environment variables are prioritized over properties.
     */
    private void setValues() {
        if (canSetValuesFromEnvironmentVariables()) {
            setValuesFromEnvironmentVariables();
        } else if (canSetValuesFromPropertiesFile()) {
            setValuesFromPropertiesFile();
        }
    }

    /**
     * Checks if it is possible to set properties from environment variables.
     *
     * @return true if it is possible to set from configuration file, false if else.
     */
    private static boolean canSetValuesFromEnvironmentVariables() {
        if (!System.getenv().containsKey(HOST_ENVIRONMENT_VARIABLE_NAME)) {
            return false;
        }

        if (!System.getenv().containsKey(PORT_ENVIRONMENT_VARIABLE_NAME)) {
            return false;
        }

        return true;
    }

    /**
     * Set properties from environment variables.
     */
    private void setValuesFromEnvironmentVariables() {
        host = System.getenv().get(HOST_ENVIRONMENT_VARIABLE_NAME);
        port = Integer.parseInt(System.getenv().get(PORT_ENVIRONMENT_VARIABLE_NAME));
    }

    /**
     * Checks if it is possible to set properties from configuration file.
     *
     * @return true if it is possible to set from configuration file, false if else.
     */
    private static boolean canSetValuesFromPropertiesFile() {
        Properties props = FileUtils.loadPropertiesFromFile(CONFIG_PROPERTIES_FILE_NAME);

        if (!props.containsKey(HOST_PROPERTIES_VARIABLE_NAME)) {
            return false;
        }

        if (!props.containsKey(PORT_PROPERTIES_VARIABLE_NAME)) {
            return false;
        }

        return true;
    }

    /**
     * Set properties from configuration file.
     */
    private void setValuesFromPropertiesFile() {
        Properties props = FileUtils.loadPropertiesFromFile(CONFIG_PROPERTIES_FILE_NAME);

        host = props.getProperty(HOST_PROPERTIES_VARIABLE_NAME);
        port = Integer.parseInt(props.getProperty(PORT_PROPERTIES_VARIABLE_NAME));
    }

    /**
     * Initialize the DataProducer.
     *
     * @return true on success, false if else
     */
    public boolean initialize() {
        setUpBootstrapGroup();

        logger.info("Connecting to: {}:{}", host, port);
        return tryToConnect();
    }

    /**
     * Set up the bootstrap group.
     */
    private void setUpBootstrapGroup() {
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new DataProducerInitializer(this));
    }

    /**
     * Tries to connect to the data receiver.
     *
     * @return true if successfully connected, false if else.
     */
    private boolean tryToConnect() {
        boolean connected = false;
        while (!connected) {
            try {
                this.channelFuture = bootstrap.connect(host, port).sync();
                connected = true;
            } catch (InterruptedException e) {
                handleConnectionInterruptedException(e);

                return false;
            } catch (Exception e) {
                handleConnectionException(e);
            }
        }

        return true;
    }

    /**
     * Handle the connection Interrupted Exception.
     *
     * @param e the exception to handle.
     */
    private void handleConnectionInterruptedException(InterruptedException e) {
        logger.warn("Connection was interrupted");
        e.printStackTrace();
        Thread.currentThread().interrupt();
    }

    /**
     * Handle the connection Exception.
     *
     * @param e the exception to handle.
     */
    private void handleConnectionException(Exception e) {
        int secondsToSleep = 5;

        e.printStackTrace();
        logger.warn(
            "Failed to initialize DataProducer, retrying in {} seconds",
            secondsToSleep
        );

        sleepForSeconds(secondsToSleep);
    }

    /**
     * Execute the DataProducer.
     */
    public void run() {
        while (this.running) {
            String line = getRandomString();
            sendMessageToReceiver(line);
            sleepForMilliseconds(1000 / messagesPerSecond);
        }
    }

    /**
     * Sends a message to the data receiver.
     *
     * @param message the message to send to the data receiver.
     */
    private void sendMessageToReceiver(String message) {
        if (this.channelFuture != null) {
            this.channelFuture = this.channelFuture.channel().writeAndFlush(message + "\r\n");
        }
    }

    /**
     * Sets the number of messages per second.
     * NOTE: Needs to be greater than 0.
     *
     * @param messagesPerSecond number of messages per second
     */
    public void setMessagesPerSecond(int messagesPerSecond) {
        if (messagesPerSecond <= 0) {
            throw new IllegalArgumentException("Number of messages per second needs to be above 0");
        }

        this.messagesPerSecond = messagesPerSecond;
    }

    /**
     * Shutdown the DataProducer.
     */
    public void shutdown() {
        this.running = false;

        logger.info("Shutting down");
        shutdownChannelFuture();
        shutdownGroup();
    }

    /**
     * Shutdown the event loop group.
     */
    private void shutdownGroup() {
        if (group != null) {
            logger.debug("Closing group");
            group.shutdownGracefully();
            group = null;
        }
    }

    /**
     * Shutdown the channel future.
     */
    private void shutdownChannelFuture() {
        if (channelFuture != null) {
            logger.debug("Closing channel future");
            channelFuture.channel().close();
            channelFuture = null;
        }
    }

    /**
     * Generate a random lorem ipsum string.
     *
     * @return a random lorem ipsum string of min <= n <= max amount of n words.
     */
    private String getRandomString() {
        String line = lorem.getWords(7, 12);
        logger.trace("String created: {}", line);

        return line;
    }

    /**
     * Sleep for a specified amount of seconds.
     *
     * @param seconds the amount of time to sleep in seconds.
     */
    private static void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sleep for a specified amount of milliseconds.
     *
     * @param milliseconds the amount of time to sleep in seconds.
     */
    private void sleepForMilliseconds(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
