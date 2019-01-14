/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016-2018 Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 */

package grakn.benchmark.runner.storage;

import grakn.core.concept.AttributeType;
import grakn.core.concept.Concept;
import grakn.core.concept.ConceptId;
import grakn.core.concept.EntityType;
import grakn.core.concept.Label;
import grakn.core.concept.RelationshipType;
import grakn.core.concept.Thing;
import grakn.core.concept.Type;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class IgniteConceptIdStoreTest {

    private IgniteConceptIdStore store;
    private HashSet<String> typeLabelsSet;
    private ArrayList<String> typeLabels;
    private ArrayList<ConceptId> conceptIds;
    private ArrayList<Concept> conceptMocks;
    private String typeLabel;

    HashSet<EntityType> entityTypes;

    @BeforeClass
    public static void initIgniteServer() throws IgniteException {
        Ignition.start();
    }

    @AfterClass
    public static void stopIgniteServer() {
        Ignition.stop(false);
    }

    @Before
    public void setUp() {

        typeLabel = "person";
        typeLabelsSet = new HashSet<>();
        typeLabelsSet.add(typeLabel);

        entityTypes = new HashSet<>();

        EntityType personEntityType = mock(EntityType.class);
        when(personEntityType.label()).thenReturn(Label.of("person"));

        entityTypes.add(personEntityType);

        conceptIds = new ArrayList<>();
        conceptIds.add(ConceptId.of("V123456"));
        conceptIds.add(ConceptId.of("V298345"));
        conceptIds.add(ConceptId.of("V380325"));
        conceptIds.add(ConceptId.of("V4"));
        conceptIds.add(ConceptId.of("V5"));
        conceptIds.add(ConceptId.of("V6"));
        conceptIds.add(ConceptId.of("V7"));

        conceptMocks = new ArrayList<>();

        Iterator<ConceptId> idIterator = conceptIds.iterator();

        while (idIterator.hasNext()) {

            // Concept
            Concept conceptMock = mock(Concept.class);
            this.conceptMocks.add(conceptMock);

            // Thing
            Thing thingMock = mock(Thing.class);
            when(conceptMock.asThing()).thenReturn(thingMock);

            // ConceptID
            ConceptId conceptId = idIterator.next();
            when(thingMock.id()).thenReturn(conceptId);

            // Concept Type
            Type conceptTypeMock = mock(Type.class);
            when(thingMock.type()).thenReturn(conceptTypeMock);

            // Concept Type label()
            Label label = Label.of(typeLabel);
            when(conceptTypeMock.label()).thenReturn(label);
        }
    }

    @Test
    public void whenConceptIdsAreAdded_conceptIdsAreInTheDB() throws SQLException {
        this.store = new IgniteConceptIdStore(entityTypes, new HashSet<RelationshipType>(), new HashSet<AttributeType>());

        // Add all of the elements
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        int counter = 0;
        // Check objects were added to the db
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + this.typeLabel)) {
                while (rs.next()) {
                    counter++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        assertEquals(7, counter);
    }

    @Test
    public void whenConceptIsAdded_conceptIdCanBeRetrieved() throws SQLException {
        this.store = new IgniteConceptIdStore(entityTypes, new HashSet<RelationshipType>(), new HashSet<AttributeType>());

        int index = 0;
        this.store.addConcept(this.conceptMocks.get(index));
        ConceptId personConceptId = this.store.getConceptId(this.typeLabel, index);
        System.out.println("Found id: " + personConceptId.toString());
        assertEquals(personConceptId, this.conceptIds.get(index));
    }

    @Test
    public void whenGettingIdWithOffset_correctIdIsReturned() throws SQLException {
        this.store = new IgniteConceptIdStore(entityTypes, new HashSet<RelationshipType>(), new HashSet<AttributeType>());

        int index = 4;
        // Add all of the elements

        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        ConceptId personConceptId = this.store.getConceptId(this.typeLabel, index);
        System.out.println("Found id: " + personConceptId.toString());
        assertEquals(this.conceptIds.get(index), personConceptId);
    }

    @Test
    public void whenCountingTypeInstances_resultIsCorrect() throws SQLException, ClassNotFoundException {
        this.store = new IgniteConceptIdStore(entityTypes, new HashSet<RelationshipType>(), new HashSet<AttributeType>());

        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        int count = this.store.getConceptCount(this.typeLabel);
        assertEquals(7, count);
    }

    @Test
    public void whenRoleplayerIsAdded_idCanBeRetrieved() throws SQLException {
        this.store = new IgniteConceptIdStore(entityTypes, new HashSet<RelationshipType>(), new HashSet<AttributeType>());

        Concept concept = conceptMocks.get(0);
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addRolePlayer(conceptMock);
        }

        int roleplayerCount = this.store.totalRolePlayers();
        assertEquals(7, roleplayerCount);
    }
}