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

package grakn.benchmark.profiler.util;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.common.exception.BootupException;
import grakn.client.GraknClient;
import graql.lang.query.GraqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static graql.lang.Graql.parseList;

public class SchemaManager {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaManager.class);

    private final BenchmarkConfiguration config;
    private final GraknClient tracingClient;

    public SchemaManager(BenchmarkConfiguration config, GraknClient tracingClient) {
        this.config = config;
        this.tracingClient = tracingClient;
    }

    public void loadSchema() {
        String keyspace = config.getKeyspace();
        if(keyspace == null) {
            throw new BootupException("No keyspace provided. Please specify --keyspace argument or add `dataGenerator` option to config file.");
        }
        if (keyspaceExists(keyspace)) {
            throw new BootupException("Keyspace " + keyspace + " already exists");
        }
        // time creation of keyspace and insertion of schema
        Span span = Tracing.currentTracer().newTrace().name("New Keyspace + schema: " + keyspace);
        span.start();
        GraknClient.Session session;
        try (Tracer.SpanInScope ws = Tracing.currentTracer().withSpanInScope(span)) {
            span.annotate("Opening new session");
            session = tracingClient.session(keyspace);
            span.annotate("Loading qraql schema");
            loadSchema(session, config.getGraqlSchema());
        }

        span.finish();
        session.close();
    }

    private void loadSchema(GraknClient.Session session, List<String> schemaQueries) {
        // load schema
        LOG.info("Loading schema in keyspace [" + session.keyspace() + "]...");
        try (GraknClient.Transaction tx = session.transaction().write()) {
            Stream<GraqlQuery> query = parseList(String.join("\n", schemaQueries));
            query.forEach(tx::execute);
            tx.commit();
        }
    }

    private boolean keyspaceExists(String keyspace){
        return tracingClient.keyspaces().retrieve().contains(keyspace);
    }
}
