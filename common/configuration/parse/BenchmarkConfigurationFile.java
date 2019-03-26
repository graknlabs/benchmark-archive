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

import java.util.List;

/**
 * Absorbs a toplevel benchmark configuration file
 */

public class BenchmarkConfigurationFile {
    private String name;
    private String description;
    private String dataGenerator;
    private String schema;
    private String queries;
    private List<Integer> scalesToProfile;
    private Integer repeatsPerQuery;

    private boolean deleteInsertedConcepts;
    private boolean traceDeleteInsertedConcepts;
    private Concurrency concurrency;

    public void setName(String name) {
        this.name= name;
    }
    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return this.description;
    }

    public void setDataGenerator(String dataGenerator) {
        this.dataGenerator = dataGenerator;
    }
    public String getDataGenerator() {
        return this.dataGenerator;
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

    public void setDeleteInsertedConcepts(Boolean deleteInsertedConcepts) {
        this.deleteInsertedConcepts = deleteInsertedConcepts;
    }
    public boolean deleteInsertedConcepts() { return deleteInsertedConcepts; }

    public void setTraceDeleteInsertedConcepts(Boolean traceDeleteInsertedConcepts) {
        this.traceDeleteInsertedConcepts = traceDeleteInsertedConcepts;
    }
    public boolean traceDeleteInsertedConcepts() { return traceDeleteInsertedConcepts; }

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


/**
 * Sub-object in yaml file that indicates concurrency configuration options
 */
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
