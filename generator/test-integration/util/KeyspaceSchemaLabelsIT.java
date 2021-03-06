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

package grakn.benchmark.generator.util;


import grakn.client.GraknClient;
import grakn.client.concept.AttributeType;
import graql.lang.Graql;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertTrue;

public class KeyspaceSchemaLabelsIT {
    private GraknClient client;
    private String keyspace;

    @Before
    public void setUp() {
        String uri = "localhost:48555";
        client = new GraknClient(uri);
        String uuid = UUID.randomUUID().toString().substring(0, 30).replace("-", "");
        keyspace = "test_" + uuid;
    }

    @After
    public void tearDown() {
        client.keyspaces().delete(keyspace);
        client.close();
    }

    @Test
    public void keyspaceSchemaLabels_shouldFetchTheCorrectLabels() {
        GraknClient.Session session = client.session(keyspace);
        GraknClient.Transaction tx = session.transaction().write();
        tx.execute(Graql.parse("define baseEntity sub entity,\n" +
                "has index," +
                "    plays Q-from," +
                "    plays Q-to;" +
                "startEntity sub baseEntity;" +
                "bulkEntity sub baseEntity;" +
                "" +
                "Q sub relation, relates Q-from, relates Q-to;\n" +
                "" +
                "index sub attribute, datatype string;").asDefine());
        tx.commit();
        session.close();


        KeyspaceSchemaLabels schemaLabels = new KeyspaceSchemaLabels(client, keyspace);
        assertThat(schemaLabels.entityLabels(), containsInAnyOrder("baseEntity", "startEntity", "bulkEntity"));
        assertThat(schemaLabels.relationLabels(), containsInAnyOrder("Q"));
        Map<String, AttributeType.DataType<?>> stringDataTypes = schemaLabels.attributeLabelsDataTypes();
        assertThat(stringDataTypes.keySet(), containsInAnyOrder("index"));
        assertTrue(stringDataTypes.containsValue(AttributeType.DataType.STRING));
    }
}