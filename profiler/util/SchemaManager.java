/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 GraknClient Labs Ltd
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

import grakn.benchmark.profiler.BootupException;
import grakn.core.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.EntityType;
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Type;
import grakn.core.server.kb.Schema;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graql.lang.Graql.parseList;
import static graql.lang.Graql.var;


/**
 * This class performs basic operations and checks on a given keyspace.
 *
 * This will be replaced by GraknClient Client when all features will be implemented in it.
 */
@SuppressWarnings("CheckReturnValue")
public class SchemaManager {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaManager.class);

    private GraknClient.Session session;
    private List<String> schemaQueries;

    public SchemaManager(GraknClient.Session session, List<String> graqlSchemaQueries) {
        this.session = session;
        this.schemaQueries = graqlSchemaQueries;
    }

    public void verifyEmptyKeyspace() {
        try (GraknClient.Transaction tx = session.transaction().read()) {
            // check for concept instances
            boolean existingConcepts = tx.stream(Graql.match(var("x").isa("thing")).get()).findFirst().isPresent();
            if (existingConcepts) {
                throw new BootupException("Keyspace [" + session.keyspace() + "] not empty, contains concept instances");
            }

            // check for schema
            List<ConceptMap> existingSchemaConcepts = tx.execute(Graql.match(var("x").sub("thing")).get());
            if (existingSchemaConcepts.size() != 4) {
                throw new BootupException("Keyspace [" + session.keyspace() + "] not empty, contains a schema");
            }
        }
    }

    public void loadSchema() {
        // load schema
        LOG.info("Initialising keyspace `" + session.keyspace() + "`...");
        try (GraknClient.Transaction tx = session.transaction().write()) {
            Stream<GraqlQuery> query = parseList(schemaQueries.stream().collect(Collectors.joining("\n")));
            query.forEach(q -> tx.execute(q));
            tx.commit();
        }
    }

    private <T extends Type> HashSet<T> getTypesOfMetaType(String metaTypeName) {
        HashSet<T> types;
        try (GraknClient.Transaction tx = session.transaction().read()) {

            GraqlGet graqlGet = Graql.match(var("x").sub(metaTypeName)).get();
            List<ConceptMap> result = tx.execute(graqlGet);

            types = result.stream()
                    .map(answer -> (T) answer.get("x").asType())
                    .filter(type -> !type.isImplicit())
                    .filter(type -> !Schema.MetaSchema.isMetaLabel(type.label()))
                    .collect(Collectors.toCollection(HashSet::new));

        }
        return types;
    }

    public HashSet<AttributeType> getAttributeTypes(){
        return getTypesOfMetaType("attribute");
    }
    public HashSet<RelationType> getRelationshipTypes(){
        return getTypesOfMetaType("relationship");
    }
    public HashSet<EntityType> getEntityTypes(){
        return getTypesOfMetaType("entity");
    }
}
