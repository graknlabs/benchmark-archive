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

package grakn.benchmark.runner.storage;

import grakn.benchmark.runner.exception.BootupException;
import grakn.core.GraknTxType;
import grakn.core.client.Grakn;
import grakn.core.concept.*;
import grakn.core.graql.*;
import grakn.core.graql.answer.ConceptMap;
import grakn.core.graql.internal.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        clearKeyspace(session);
        try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
            tx.graql().parser().parseList(graqlSchemaQueries.stream().collect(Collectors.joining("\n"))).forEach(Query::execute);
            tx.commit();
        }
    }

    private static void clearKeyspace(Grakn.Session session) {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
            // delete all attributes, relationships, entities from keyspace

            QueryBuilder qb = tx.graql();
            Var x = Graql.var().asUserDefined();  //TODO This needed to be asUserDefined or else getting error: ai.grakn.exception.GraqlQueryException: the variable $1528883020589004 is not in the query
            Var y = Graql.var().asUserDefined();

            // TODO Sporadically has errors, logged in bug #20200

            // cannot use delete "thing", complains
            qb.match(x.isa("attribute")).delete(x).execute();
            qb.match(x.isa("relationship")).delete(x).execute();
            qb.match(x.isa("entity")).delete(x).execute();

            //
//            qb.undefine(y.sub("thing")).execute(); // TODO undefine $y sub thing; doesn't work/isn't supported
            // TODO undefine $y sub entity; also doesn't work, you need to be specific with undefine

            List<ConceptMap> schema = qb.match(y.sub("thing")).get().execute();

            for (ConceptMap element : schema) {
                Var z = Graql.var().asUserDefined();
                qb.undefine(z.id(element.get(y).id())).execute();
            }

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

    public static boolean isTypeLabelAttribute(Grakn.Transaction tx, String label) {
        SchemaConcept concept= tx.getSchemaConcept(Label.of(label));
        return concept.isAttributeType();
    }

    public static Class getAttributeDatatype(Grakn.Transaction tx, String label) throws ClassNotFoundException {
        SchemaConcept concept = tx.getSchemaConcept(Label.of(label));
        String name = concept.asAttributeType().dataType().getName();
        return Class.forName(name);
    }
}
