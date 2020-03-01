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

package grakn.benchmark.profiler.usecase;

import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.profiler.ThreadedProfiler;
import grakn.benchmark.profiler.util.SchemaManager;
import grakn.client.GraknClient;

import java.util.Collections;
import java.util.List;

public class LoadSchema implements UseCase {
    private final BenchmarkConfiguration config;
    private final GraknClient client;
    private final SchemaManager schemaManager;

    LoadSchema(BenchmarkConfiguration config, GraknClient client, SchemaManager schemaManager) {
        this.config = config;
        this.client = client;
        this.schemaManager = schemaManager;
    }

    @Override
    public void run() {
        schemaManager.loadSchema();
        List<String> keyspaces = Collections.singletonList(config.getKeyspace());
        int numConcepts = 0;

        ThreadedProfiler threadedProfiler = new ThreadedProfiler(client, keyspaces, config);
        threadedProfiler.processQueries(config.numQueryRepetitions(), numConcepts);
        threadedProfiler.cleanup();
    }
}
