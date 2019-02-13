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
    private Transaction transaction;
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

    // --- transaction configs ---
    public void setTransaction(Transaction tx) {
        transaction = tx;
    }
    public Integer queriesPerTx() {
        return transaction.getQueriesPerTx();
    }

    // --- concurrency configs ---
    public void setConcurrency(Concurrency concurrency) {
        this.concurrency = concurrency;
    }
    public Integer concurrentSessions() {
        return this.concurrency.sessions();
    }
    public Boolean separateConcurrentKeyspaces() {
        return this.concurrency.separateKeyspaces();
    }

}

class Transaction {
    private Integer queriesPerTx;
    public void setQueriesPerTx() {
        this.queriesPerTx = queriesPerTx;
    }
    public Integer getQueriesPerTx() {
        return queriesPerTx;
    }
}

class Concurrency {
    private Integer sessions;
    private Boolean separateKeyspaces;

    private void setSessions(Integer sessions) {
        this.sessions = sessions;
    }
    public Integer sessions() {
        return sessions;
    }

    private void setSeparateKeyspaces(Boolean separateKeyspaces) {
        this.separateKeyspaces = separateKeyspaces;
    }
    public Boolean separateKeyspaces() {
        return separateKeyspaces;
    }
}