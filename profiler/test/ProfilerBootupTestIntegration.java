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

package grakn.benchmark.profiler;

import grakn.benchmark.common.configuration.parse.BenchmarkArguments;
import grakn.benchmark.common.exception.BootupException;
import grakn.core.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static graql.lang.Graql.type;
import static graql.lang.Graql.var;

public class ProfilerBootupTestIntegration {
    private final static Path WEB_CONTENT_CONFIG_PATH = Paths.get("profiler/test/resources/web_content/web_content_config_test.yml");

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
        client.keyspaces().delete(keyspace);
        session.close();
    }

    @Test
    public void whenProvidingAbsolutePathToExistingConfig_benchmarkShouldStart() {
        String[] args = new String[]{"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--execution-name", "grakn-benchmark-test"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
    }

    @Test
    public void whenProvidingRelativePathToExistingConfig_benchmarkShouldStart() {
        String[] args = new String[]{"--config", "web_content_config_test.yml", "--execution-name", "grakn-benchmark-test"};
        System.setProperty("working.dir", WEB_CONTENT_CONFIG_PATH.getParent().toString());
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
    }

    @Test
    public void whenSchemaExistsInKeyspace_throwException() {

        try (GraknClient.Transaction tx = session.transaction().write()) {
            List<ConceptMap> answer = tx.execute(Graql.define(type("person").sub("entity")));
            tx.commit();
        }

        expectedException.expect(BootupException.class);
        expectedException.expectMessage("is not empty");
        String[] args = new String[]{"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--keyspace", keyspace, "--execution-name", "testing"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
        graknBenchmark.start();
    }

    @Test
    public void whenDataExistsInKeyspace_throwException() {

        try (GraknClient.Transaction tx = session.transaction().write()){
            List<ConceptMap> answer = tx.execute(Graql.define(type("person").sub("entity")));
            answer = tx.execute(Graql.insert(var("x").isa("person")));
            tx.commit();
        }

        expectedException.expect(BootupException.class);
        expectedException.expectMessage("is not empty");
        String[] args = new String[] {"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--keyspace", keyspace, "--execution-name", "testing"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
        graknBenchmark.start();
    }
}
