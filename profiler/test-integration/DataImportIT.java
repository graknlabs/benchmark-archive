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
import grakn.client.GraknClient;
import grakn.client.answer.Numeric;
import graql.lang.Graql;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;

public class DataImportIT {
    private final static Path WEB_CONTENT_DATA_IMPORT_CONFIG_PATH = Paths.get("profiler/test-integration/resources/web_content/web_content_config_data_import.yml");

    private GraknClient client;
    private String keyspace;

    @Before
    public void setUp() {
        String uri = "localhost:48555";
        client = new GraknClient(uri);
        String uuid = UUID.randomUUID().toString().substring(0, 30).replace("-", "");
        keyspace = "test_" + uuid;
    }

    @After
    public void tearDown() {
        client.keyspaces().delete(keyspace);
        client.close();
    }

    @Test
    public void whenUsingDataImportFile_graknContainsData() {
        String[] args = new String[] {
                "--config", WEB_CONTENT_DATA_IMPORT_CONFIG_PATH.toString(),
                "--keyspace", keyspace,
                "--execution-name", "testing",
                "--" + BenchmarkArguments.LOAD_SCHEMA_ARGUMENT,
                "--" + BenchmarkArguments.STATIC_DATA_IMPORT_ARGUMENT};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
        graknBenchmark.start();

        GraknClient.Session session = client.session(keyspace);
        GraknClient.Transaction tx = session.transaction().read();
            List<Numeric> answer = tx.execute(Graql.parse("match $x isa thing; get; count;").asGetAggregate());
            assertTrue(answer.get(0).number().intValue() > 0);
        tx.close();
        session.close();
    }
}
