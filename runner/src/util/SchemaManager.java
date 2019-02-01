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

package grakn.benchmark.runner.util;

import grakn.benchmark.runner.exception.BootupException;
import grakn.core.GraknTxType;
import grakn.core.client.Grakn;
import grakn.core.concept.*;
import grakn.core.graql.*;
import grakn.core.graql.answer.ConceptMap;
import grakn.core.graql.internal.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static grakn.core.graql.internal.pattern.Patterns.var;

/**
 *
 */
@SuppressWarnings("CheckReturnValue")
public class SchemaManager {

    public static void verifyEmptyKeyspace(Grakn.Session session) {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            // check for concept instances
            List<ConceptMap> existingConcepts = tx.graql().match(var("x").isa("thing")).limit(1).get().execute();
            if (existingConcepts.size() != 0) {
                throw new BootupException("Keyspace [" + session.keyspace() + "] not empty, contains concept instances");
            }

            // check for schema
            List<ConceptMap> existingSchemaConcepts = tx.graql().match(var("x").sub("thing")).get().execute();
            if (existingSchemaConcepts.size() != 4) {
                throw new BootupException("Keyspace [" + session.keyspace() + "] not empty, contains a schema");
            }
        }
    }

    public static void initialiseKeyspace(Grakn.Session session, List<String> graqlSchemaQueries) {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
            tx.graql().parser().parseList(graqlSchemaQueries.stream().collect(Collectors.joining("\n"))).forEach(Query::execute);
            tx.commit();
        }
    }

    public static <T extends Type> HashSet<T> getTypesOfMetaType(Grakn.Transaction tx, String metaTypeName) {
        QueryBuilder qb = tx.graql();
        Match match = qb.match(var("x").sub(metaTypeName));
        List<ConceptMap> result = match.get().execute();

        return result.stream()
                .map(answer -> (T) answer.get(var("x")).asType())
                .filter(type -> !type.isImplicit())
                .filter(type -> !Schema.MetaSchema.isMetaLabel(type.label()))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
