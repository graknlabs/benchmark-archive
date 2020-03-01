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

package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import grakn.client.concept.Label;
import grakn.client.concept.Type;
import grakn.core.rule.GraknTestServer;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertThat;

public class SchemaWalkerIT {

    private static final String testKeyspace = "schemawalker_test";

    @ClassRule
    public static final GraknTestServer server = new GraknTestServer(
            Paths.get("querygen/test-integration/conf/grakn.properties"),
            Paths.get("querygen/test-integration/conf/cassandra-embedded.yaml")
    );

    @BeforeClass
    public static void loadSchema() {
        Path path = Paths.get("querygen");
        GraknClient client = new GraknClient(server.grpcUri());
        GraknClient.Session session = client.session(testKeyspace);
        GraknClient.Transaction transaction = session.transaction().write();

        try {
            List<String> lines = Files.readAllLines(Paths.get("querygen/test-integration/resources/schema.gql"));
            String graqlQuery = String.join("\n", lines);
            transaction.execute((GraqlQuery) Graql.parse(graqlQuery));
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        session.close();
        client.close();
    }

    @Test
    public void walkSubRetrievesDifferentSubtypes() {
        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace);
             GraknClient.Transaction tx = session.transaction().write()) {

            Type rootThing = tx.getMetaConcept();
            Random random = new Random(0);

            List<Type> subTypes = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                subTypes.add(SchemaWalker.walkSubs(rootThing, random));
            }

            assertThat(subTypes, CoreMatchers.not(CoreMatchers.everyItem(CoreMatchers.is(rootThing))));
        }
    }


    @Test
    public void walkSupsNoMetaDoesNotRetrieveMetaTypes() {
        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace);
             GraknClient.Transaction tx = session.transaction().write()) {

            Type personType = tx.getSchemaConcept(Label.of("person"));
            Random random = new Random(0);

            List<Type> superTypes = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                superTypes.add(SchemaWalker.walkSupsNoMeta(tx, personType, random));
            }

            assertThat(superTypes, CoreMatchers.not(CoreMatchers.anyOf(
                    CoreMatchers.is(tx.getMetaAttributeType()),
                    CoreMatchers.is(tx.getMetaRelationType()),
                    CoreMatchers.is(tx.getMetaEntityType()),
                    CoreMatchers.is(tx.getMetaConcept())
            )));


        }
    }
}
