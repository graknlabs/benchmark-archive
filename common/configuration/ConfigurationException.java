package grakn.benchmark.common.configuration;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause, false, false);
    }

    public ConfigurationException(String message){
        super(message, null, false, false);
    }
}
