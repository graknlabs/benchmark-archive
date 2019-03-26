/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.common.configuration.parse;

import grakn.benchmark.common.configuration.ConfigurationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Helper class used to parse console arguments into predefined options
 */
public class BenchmarkArguments {

    public final static String CONFIG_ARGUMENT = "config";
    public final static String GRAKN_URI = "grakn-uri";
    public final static String KEYSPACE_ARGUMENT = "keyspace";
    public final static String NO_DATA_GENERATION_ARGUMENT = "no-data-generation";
    public final static String LOAD_SCHEMA_ARGUMENT = "load-schema";
    public final static String EXECUTION_NAME_ARGUMENT = "execution-name";
    public final static String ELASTIC_URI = "elastic-uri";

    public static CommandLine parse(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine arguments = parser.parse(options, args);
            return arguments;
        } catch (ParseException e) {
            throw new ConfigurationException(e.getMessage(), e.getCause());
        }
    }

    private static Options buildOptions() {
        Option configFileOption = Option.builder("c")
                .longOpt(CONFIG_ARGUMENT)
                .hasArg(true)
                .desc("Benchmarking YAML file (required)")
                .required(true)
                .type(String.class)
                .build();
        Option executionNameOption = Option.builder("n")
                .longOpt(EXECUTION_NAME_ARGUMENT)
                .hasArg(true)
                .required(true)
                .desc("Name for specific execution, this label is passed through to the storage backend")
                .type(String.class)
                .build();
        Option graknAddressOption = Option.builder("u")
                .longOpt(GRAKN_URI)
                .hasArg(true)
                .desc("Address of the grakn cluster (default: localhost:48555)")
                .required(false)
                .type(String.class)
                .build();

        Option keyspaceOption = Option.builder("k")
                .longOpt(KEYSPACE_ARGUMENT)
                .required(false)
                .hasArg(true)
                .desc("Specific keyspace to utilize (default: `name` in config yaml")
                .type(String.class)
                .build();
        Option noDataGenerationOption = Option.builder("ng")
                .longOpt(NO_DATA_GENERATION_ARGUMENT)
                .required(false)
                .desc("Disable data generation")
                .type(Boolean.class)
                .build();
        Option loadSchema = Option.builder("ls")
                .longOpt(LOAD_SCHEMA_ARGUMENT)
                .required(false)
                .desc("Load a schema, even if data generation is disabled")
                .type(Boolean.class)
                .build();
        Option elasticsearchAddressOption = Option.builder("e")
                .longOpt(ELASTIC_URI)
                .hasArg(true)
                .desc("Address of the elastic search server (default: localhost:9200")
                .required(false)
                .type(String.class)
                .build();
        Options options = new Options();
        options.addOption(configFileOption);
        options.addOption(graknAddressOption);
        options.addOption(keyspaceOption);
        options.addOption(noDataGenerationOption);
        options.addOption(loadSchema);
        options.addOption(executionNameOption);
        options.addOption(elasticsearchAddressOption);
        return options;
    }
}
