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

package grakn.benchmark.profiler;

import grakn.benchmark.common.configuration.parse.BenchmarkArguments;
import grakn.benchmark.common.exception.BootupException;
import grakn.client.GraknClient;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ProfilerBootupIT {
    private final static Path WEB_CONTENT_DATA_GEN_CONFIG_PATH = Paths.get("profiler/test-integration/resources/web_content/web_content_config_data_gen.yml");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    private GraknClient client;
    private GraknClient.Session session;
    private String keyspace;

    @Before
    public void setUp() {
        String uri = "localhost:48555";
        client = new GraknClient(uri);
        String uuid = UUID.randomUUID().toString().substring(0, 30).replace("-", "");
        keyspace = "test_" + uuid;
        session = client.session(keyspace);
    }

    @After
    public void tearDown() {
        session.close();
        client.keyspaces().delete(keyspace);
        client.close();
    }

    @Test
    public void whenKeyspaceAlreadyExists_throwException() {
        GraknClient.Transaction tx = session.transaction().read();
        tx.close();
        String[] args = new String[]{"--config", WEB_CONTENT_DATA_GEN_CONFIG_PATH.toAbsolutePath().toString(), "--keyspace", keyspace, "--execution-name", "testing"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);

        expectedException.expect(BootupException.class);
        expectedException.expectMessage("already exists");
        graknBenchmark.start();
    }
}
