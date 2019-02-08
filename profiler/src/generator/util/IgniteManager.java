package grakn.benchmark.profiler.generator.util;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;

public class IgniteManager {

    public static Ignite initIgnite() {
        System.setProperty("IGNITE_QUIET", "false"); // When Ignite is in quiet mode forces all the output to System.out, we don't want that
        System.setProperty("IGNITE_NO_ASCII", "true"); // Disable Ignite ASCII logo
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true"); // Enable suggestions when need performance improvements
        System.setProperty("java.net.preferIPv4Stack", "true"); // As suggested by Ignite we set preference on IPv4
        IgniteConfiguration igniteConfig = new IgniteConfiguration();
        IgniteLogger logger = new Slf4jLogger();
        igniteConfig.setGridLogger(logger);
        return Ignition.start(igniteConfig);
    }
}
