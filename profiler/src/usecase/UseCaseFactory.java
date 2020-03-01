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
import grakn.benchmark.profiler.util.SchemaManager;
import grakn.client.GraknClient;

public class UseCaseFactory {
    private final GraknClient tracingClient;
    private final SchemaManager schemaManager;

    public UseCaseFactory(GraknClient tracingClient, SchemaManager schemaManager) {
        this.tracingClient = tracingClient;
        this.schemaManager = schemaManager;
    }

    public UseCase create(BenchmarkConfiguration config) {
        if (config.generateData()) {
            return new LoadSchemaGenerateData(config, tracingClient, schemaManager);
        }
        if (config.loadSchema() && config.staticDataImport()) {
            return new LoadSchemaLoadData(config, tracingClient, schemaManager);
        }
        if (config.loadSchema()) {
            return new LoadSchema(config, tracingClient, schemaManager);
        }
        return new ProfileExisting(config, tracingClient);
    }
}
