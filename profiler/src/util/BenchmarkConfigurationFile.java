/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
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

package grakn.benchmark.profiler.util;

import java.util.List;

/**
 * Absorbs a toplevel benchmark configuration file
 */

public class BenchmarkConfigurationFile {
    private String graphName;
    private String schema;
    private String queries;
    private List<Integer> scalesToProfile;
    private Integer repeatsPerQuery;

    private boolean commitQueries;
    private Concurrency concurrency;

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }
    public String getGraphName() {
        return this.graphName;
    }

    public void setSchema(String schemaFile) {
        this.schema = schemaFile;
    }
    public String getRelativeSchemaFile() {
        return this.schema;
    }

    public void setQueries(String queriesYaml) {
        this.queries = queriesYaml;
    }
    public String getQueriesFilePath() {
        return this.queries;
    }

    public void setCommitQueries(Boolean commitQueries) {
        this.commitQueries = commitQueries;
    }
    public Boolean commitQueries() {
        return commitQueries;
    }

    public void setScales(List<Integer> scales) {
        this.scalesToProfile = scales;
    }
    public List<Integer> scalesToProfile() {
        return this.scalesToProfile;
    }

    public void setRepeatsPerQuery(Integer repeatsPerQuery) {
        this.repeatsPerQuery = repeatsPerQuery;
    }
    public int getRepeatsPerQuery() {
        return this.repeatsPerQuery;
    }

    // --- concurrency configs ---
    public void setConcurrency(Concurrency concurrency) {
        this.concurrency = concurrency;
    }
    public Integer concurrentClients() {
        return this.concurrency.clients();
    }
    public Boolean uniqueConcurrentKeyspaces() {
        return this.concurrency.uniqueKeyspaces();
    }
}


class Concurrency {
    private Integer clients;
    private Boolean separateKeyspaces;

    private void setClients(Integer clients) {
        this.clients= clients;
    }
    public Integer clients() {
        return clients;
    }

    private void setUniqueKeyspaces(Boolean uniqueKeyspaces) {
        this.separateKeyspaces = uniqueKeyspaces;
    }
    public Boolean uniqueKeyspaces() {
        return separateKeyspaces;
    }
}