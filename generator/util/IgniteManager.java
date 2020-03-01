/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.benchmark.generator.util;

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
