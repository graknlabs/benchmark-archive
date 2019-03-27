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

package grakn.benchmark.generator.util;

import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.Type;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static graql.lang.Graql.var;


/**
 * This class performs basic operations and checks on a given keyspace.
 * <p>
 * This will be replaced by Client when all features will be implemented in it.
 */
@SuppressWarnings("CheckReturnValue")
public class SchemaManager {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaManager.class);
    private static final Set<String> META_CONCEPT_LABELS = new HashSet<>(Arrays.asList("thing", "entity", "role", "attribute", "relation", "rule"));

    private GraknClient.Session session;

    public SchemaManager(GraknClient.Session session) {
        this.session = session;
    }

    public boolean verifyEmptyKeyspace() {
        try (GraknClient.Transaction tx = session.transaction().read()) {
            // check for concept instances
            boolean instancesExist = tx.stream(Graql.match(var("x").isa("thing")).get()).findFirst().isPresent();

            // check for schema
            List<ConceptMap> existingSchemaConcepts = tx.execute(Graql.match(var("x").sub("thing")).get());

            // keyspace is "empty" is we have no instances and exactly four meta-concepts (thing, relation, entity, attribute)
            return !instancesExist && existingSchemaConcepts.size() == 4;
        }
    }


    private HashSet<String> getLabelsOfMetaType(String metaTypeName) {
        HashSet<Type> types;
        HashSet<String> typeLabels = new HashSet<>();
        try (GraknClient.Transaction tx = session.transaction().read()) {

            GraqlGet graqlGet = Graql.match(var("x").sub(metaTypeName)).get();
            List<ConceptMap> result = tx.execute(graqlGet);

            types = result.stream()
                    .map(answer -> answer.get("x").asType())
                    .filter(type -> !type.isImplicit())
                    .filter(type -> !META_CONCEPT_LABELS.contains(type.label().getValue()))
                    .collect(Collectors.toCollection(HashSet::new));


            for (Type conceptType : types) {
                typeLabels.add(conceptType.label().toString());
            }
        }
        return typeLabels;
    }

    private HashMap<String, AttributeType.DataType<?>> getAttributeTypeLabels(String metaTypeName) {
        HashSet<AttributeType> types;
        HashMap<String, AttributeType.DataType<?>> typeLabels = new HashMap<>();
        try (GraknClient.Transaction tx = session.transaction().read()) {

            GraqlGet graqlGet = Graql.match(var("x").sub(metaTypeName)).get();
            List<ConceptMap> result = tx.execute(graqlGet);

            types = result.stream()
                    .map(answer -> answer.get("x").asAttributeType())
                    .filter(type -> !type.isImplicit())
                    .filter(type -> !META_CONCEPT_LABELS.contains(type.label().getValue()))
                    .collect(Collectors.toCollection(HashSet::new));


            for (AttributeType conceptType : types) {
                String label = conceptType.label().toString();
                AttributeType.DataType<?> datatype = conceptType.dataType();
                typeLabels.put(label, datatype);
            }
        }

        return typeLabels;
    }

    public HashMap<String, AttributeType.DataType<?>> getAttributeTypes() {
        return getAttributeTypeLabels("attribute");
    }

    public HashSet<String> getRelationTypes() {
        return getLabelsOfMetaType("relation");
    }

    public HashSet<String> getEntityTypes() {
        return getLabelsOfMetaType("entity");
    }
}
