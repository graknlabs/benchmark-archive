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

package grakn.benchmark.common.configuration;

import grakn.benchmark.common.configuration.parse.BenchmarkArguments;
import org.apache.commons.cli.CommandLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 *
 */
public class ConfigurationTest {
    private final static Path WEB_CONTENT_DATA_GEN_CONFIG_PATH = Paths.get("common/configuration/test/resources/web_content/web_content_config_data_gen.yml");
    private final static Path WEB_CONTENT_DATA_IMPORT_CONFIG_PATH = Paths.get("common/configuration/test/resources/web_content/web_content_config_data_import.yml");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenProvidingAbsolutePathToNonExistingConfig_throwException() {
        /*
        When providing an invalid configuration cmd flag, benchmark should throw an exception
         */
        String[] args = new String[]{"--config", "nonexistingpath", "--execution-name", "grakn-benchmark-test"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("The provided config file");
        expectedException.expectMessage("nonexistingpath");
        expectedException.expectMessage("does not exist");
        BenchmarkConfiguration configuration = new BenchmarkConfiguration(commandLine);
    }

    @Test
    public void whenConfigurationArgumentNotProvided_throwException() {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Missing required option: c");
        BenchmarkArguments.parse(new String[]{"--execution-name", "grakn-benchmark-test"});
    }

    @Test
    public void whenExecutionNameArgumentNotProvided_throwException() {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Missing required option: n");
        BenchmarkArguments.parse(new String[]{"--config", "web_content_config_data_gen.yml"});
    }

    @Test
    public void whenProvidingValidStaticQueriesFile_noExceptionThrown() {
        String[] args = new String[]{"--config", WEB_CONTENT_DATA_IMPORT_CONFIG_PATH.toAbsolutePath().toString(), "--execution-name", "grakn-benchmark-test", "--static-data-import"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        BenchmarkConfiguration config = new BenchmarkConfiguration(commandLine);
    }

    @Test
    public void whenProvidingAbsolutePathToConfig_noExceptionThrown() {
        String[] args = new String[]{"--config", WEB_CONTENT_DATA_GEN_CONFIG_PATH.toAbsolutePath().toString(), "--execution-name", "grakn-benchmark-test"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        BenchmarkConfiguration config = new BenchmarkConfiguration(commandLine);
    }

    @Test
    public void whenProvidingRelativePathToExistingConfig_noExceptionThrown() {
        String[] args = new String[]{"--config", "web_content_config_data_gen.yml", "--execution-name", "grakn-benchmark-test"};
        System.setProperty("working.dir", WEB_CONTENT_DATA_GEN_CONFIG_PATH.getParent().toString());
        CommandLine commandLine = BenchmarkArguments.parse(args);
        BenchmarkConfiguration config = new BenchmarkConfiguration(commandLine);
    }

}