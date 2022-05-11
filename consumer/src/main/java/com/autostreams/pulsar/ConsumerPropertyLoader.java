package com.autostreams.pulsar;

import com.autostreams.utils.fileutils.FileUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Represents an object responsible for handling and loading configurations for the consumer.
 *
 * @version 1.0
 * @since 1.0
 */
public class ConsumerPropertyLoader {
    private static final String CONFIG_NAME = "consumerconfig.properties";
    private final ArrayList<ConfigurationNamePair> configVariableKeys = new ArrayList<>();
    private final Set<String> topics = new HashSet<>();
    private static final String HOST_PROPERTY_VARIABLE_NAME = "url";
    private static final String HOST_ENVIRONMENT_VARIABLE_NAME = "PULSAR_BROKER_URL";

    private record ConfigurationNamePair(String propertyVariableName,
                                         String environmentVariableName) {
    }

    /**
     * Gets the available consumer configuration. Environment variables are prioritized over
     * the .properties file, meaning that if both exist the environment variables will be used.
     *
     * @return String-Object map containing configuration keys as strings and their values as
     *         objects
     */
    public Map<String, Object> getConsumerConfiguration() {
        this.setConfigVariableKeys();

        HashMap<String, Object> consumerConfiguration;
        if (canSetValuesFromEnvironmentVariables()) {
            consumerConfiguration = this.getConfigurationFromEnvironment();
        } else {
            Properties properties = FileUtils.loadPropertiesFromFile(CONFIG_NAME);
            consumerConfiguration = this.getConfigurationFromPropertiesFile(properties);
        }

        return consumerConfiguration;
    }

    /**
     * Sets keys for the .properties file and environment variables.
     */
    private void setConfigVariableKeys() {
        this.addPropertyPair("topicNames", "TOPIC_NAME");
        this.addPropertyPair("subscriptionName", "SUBSCRIPTION_NAME");
        this.addPropertyPair("consumerName", "CONSUMER_NAME");
        this.addPropertyPair("subscriptionType", "SUBSCRIPTION_TYPE");
        this.addPropertyPair("receiverQueueSize", "RECEIVER_QUEUE_SIZE");
        this.addPropertyPair("acknowledgementsGroupTimeMicros",
                "ACKNOWLEDGEMENTS_GROUP_TIME_MICROS ");
        this.addPropertyPair("ackTimeoutMillis", "ACK_TIMEOUT_MILLIS");
        this.addPropertyPair("tickDurationMillis", "TICK_DURATION_MILLIS");
    }

    /**
     * Adds a pair of variable names to the collection of variable names.
     *
     * @param propertyVariableName .properties variety of variable name
     * @param environmentVariableName Environment variable variety of variable name
     */
    private void addPropertyPair(String propertyVariableName, String environmentVariableName) {
        ConfigurationNamePair pairToAdd = new ConfigurationNamePair(propertyVariableName,
                environmentVariableName);

        this.configVariableKeys.add(pairToAdd);
    }

    /**
     * Attempts to set the host string based on available configurations.
     */
    public String getHost() {
        String host;
        Properties properties = FileUtils.loadPropertiesFromFile(CONFIG_NAME);

        if (canSetValueFromEnvironmentVariable(HOST_ENVIRONMENT_VARIABLE_NAME)) {
            host = System.getenv().get(HOST_ENVIRONMENT_VARIABLE_NAME);
        } else {
            host = properties.getProperty(HOST_PROPERTY_VARIABLE_NAME);
        }

        return host;
    }

    /**
     * Checks a variable and evaluates whether the variable exists as an environment variable.
     *
     * @param variableName name of variable to check
     * @return true if variable exists as environment variable, otherwise false
     */
    private boolean canSetValueFromEnvironmentVariable(String variableName) {
        return System.getenv().containsKey(variableName);
    }

    private boolean canSetValuesFromEnvironmentVariables() {
        boolean valid = true;
        int index = 0;

        while (valid && index < this.configVariableKeys.size()) {
            String environmentValueName = this
                    .configVariableKeys
                    .get(index)
                    .environmentVariableName;
            if (!this.canSetValueFromEnvironmentVariable(environmentValueName)) {
                valid = false;
            }
            index++;
        }

        return valid;
    }

    /**
     * Gets the consumer worker configuration from environment variables.
     *
     * @return configuration as String-Object map
     */
    private HashMap<String, Object> getConfigurationFromEnvironment() {
        HashMap<String, Object> result = new HashMap<>();

        for (ConfigurationNamePair configNamePair : this.configVariableKeys) {
            String environmentVariableName = configNamePair.environmentVariableName;
            String propertyVariableName = configNamePair.propertyVariableName;

            Object configurationValue = System
                    .getenv()
                    .get(environmentVariableName);

            putVariableToResult(propertyVariableName, configurationValue, result);
        }

        return result;
    }

    /**
     * Gets configuration from loaded properties.
     *
     * @param properties loaded properties for the worker
     * @return configuration as String-Object map
     */
    private HashMap<String, Object> getConfigurationFromPropertiesFile(Properties properties) {
        HashMap<String, Object> result = new HashMap<>();

        for (ConfigurationNamePair configNamePair : this.configVariableKeys) {
            String propertyVariableName = configNamePair.propertyVariableName;
            Object configurationValue = properties.get(propertyVariableName);

            putVariableToResult(propertyVariableName, configurationValue, result);
        }

        return result;
    }

    private void putVariableToResult(String propertyVariableName,
                                     Object configurationValue,
                                     HashMap<String, Object> result) {

        if (propertyVariableName.equals("topicNames")) {
            this.topics.add(String.valueOf(configurationValue));
            result.put(propertyVariableName, this.topics);
        } else {
            result.put(propertyVariableName, configurationValue);
        }
    }
}
