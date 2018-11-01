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

package storage;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.concept.Label;
import ai.grakn.concept.Role;
import ai.grakn.concept.SchemaConcept;
import ai.grakn.concept.Type;
import ai.grakn.graql.Graql;
import ai.grakn.graql.Match;
import ai.grakn.graql.Query;
import ai.grakn.graql.QueryBuilder;
import ai.grakn.graql.Var;
import ai.grakn.graql.answer.ConceptMap;
import ai.grakn.util.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static ai.grakn.graql.internal.pattern.Patterns.var;

/**
 *
 */
public class SchemaManager {


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

    public static HashSet<Role> getRoles(Grakn.Transaction tx, String metaTypeName) {
        QueryBuilder qb = tx.graql();
        Match match = qb.match(var("x").sub(metaTypeName));
        List<ConceptMap> result = match.get().execute();

        return result.stream()
                .map(answer -> answer.get(var("x")).asRole())
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
