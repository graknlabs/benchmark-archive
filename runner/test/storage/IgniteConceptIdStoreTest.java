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

import grakn.core.concept.*;
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
    private String entityTypeLabel;
    HashSet<EntityType> entityTypes;

    private String attrTypeLabel;
    HashSet<AttributeType> attributeTypes;

    private String relTypeLabel;
    HashSet<RelationshipType> relationshipTypes;

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

        //  --- Entities ---

        entityTypeLabel = "person";
        typeLabelsSet = new HashSet<>();
        typeLabelsSet.add(entityTypeLabel);
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
            Label label = Label.of(entityTypeLabel);
            when(conceptTypeMock.label()).thenReturn(label);
        }

        // --- attributes ---

        attrTypeLabel = "age";
        typeLabelsSet.add(attrTypeLabel);
        attributeTypes = new HashSet<>();
        AttributeType ageAttributeType = mock(AttributeType.class);
        when(ageAttributeType.label()).thenReturn(Label.of(attrTypeLabel));
        when(ageAttributeType.dataType()).thenReturn(AttributeType.DataType.LONG); // Data Type
        attributeTypes.add(ageAttributeType);

        Concept conceptMock = mock(Concept.class);
        conceptMocks.add(conceptMock);
        Thing thingMock = mock(Thing.class);
        when(conceptMock.asThing()).thenReturn(thingMock); // Thing
        when(thingMock.id()).thenReturn(ConceptId.of("V8")); // Concept Id
        conceptIds.add(thingMock.id());
        Type conceptTypeMock = mock(Type.class);
        when(thingMock.type()).thenReturn(conceptTypeMock); // Concept Type
        when(conceptTypeMock.label()).thenReturn(Label.of(attrTypeLabel)); // Type label
        Attribute<Long> attributeMock = mock(Attribute.class);
        when(conceptMock.<Long>asAttribute()).thenReturn(attributeMock); // Concept -> Attribute<Long>
        when(attributeMock.value()).thenReturn(10l); // Attribute Value


        // --- relationships ---
        relTypeLabel = "friend";
        typeLabelsSet.add(relTypeLabel);
        relationshipTypes = new HashSet<>();
        RelationshipType friendRelationshipType = mock(RelationshipType.class);
        when(friendRelationshipType.label()).thenReturn(Label.of("friend"));
        relationshipTypes.add(friendRelationshipType);

        Concept relConceptMock = mock(Concept.class);
        conceptMocks.add(relConceptMock);
        Thing relThingMock = mock(Thing.class);
        when(relConceptMock.asThing()).thenReturn(relThingMock); // Thing
        when(relThingMock.id()).thenReturn(ConceptId.of("V9")); // Concept Id
        conceptIds.add(relThingMock.id());
        Type relConceptTypeMock = mock(Type.class);
        when(relThingMock.type()).thenReturn(relConceptTypeMock); // Concept Type
        when(relConceptTypeMock.label()).thenReturn(Label.of("friend")); // Type label

    }

    @Test
    public void whenConceptIdsAreAdded_conceptIdsAreInTheDB() throws SQLException {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        // Add all of the elements
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        int counter = 0;
        // Check objects were added to the db
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + this.entityTypeLabel)) {
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
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        int index = 0;
        this.store.addConcept(this.conceptMocks.get(index));
        ConceptId personConceptId = this.store.getConceptId(this.entityTypeLabel, index);
        System.out.println("Found id: " + personConceptId.toString());
        assertEquals(personConceptId, this.conceptIds.get(index));
    }

    @Test
    public void whenGettingIdWithOffset_correctIdIsReturned() throws SQLException {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        int index = 4;
        // Add all of the elements

        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        ConceptId personConceptId = this.store.getConceptId(this.entityTypeLabel, index);
        System.out.println("Found id: " + personConceptId.toString());
        assertEquals(this.conceptIds.get(index), personConceptId);
    }

    @Test
    public void whenCountingTypeInstances_resultIsCorrect() throws SQLException, ClassNotFoundException {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        int count = this.store.getConceptCount(this.entityTypeLabel);
        assertEquals(7, count);
    }

    @Test
    public void whenRolePlayerIsAdded_countIsCorrect() {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        for (Concept conceptMock : this.conceptMocks) {
            this.store.addRolePlayer(conceptMock);
        }

        int roleplayerCount = this.store.totalRolePlayers();
        assertEquals(9, roleplayerCount);
    }

    @Test
    public void whenAllButOnePlayingRole_orphanEntitiesCorrect() {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // add 6 of 7 entities as role players too
        for (int i = 0 ; i < 6; i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            this.store.addRolePlayer(conceptMock);
        }

        int orphanEntities = this.store.totalOrphanEntities();
        assertEquals(orphanEntities, 1);
    }

    @Test
    public void whenAllButOnePlayingRole_orphanAttributesCorrect() {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // ad all but the attribute and relationship
        for (int i = 0 ; i < conceptMocks.size()-2; i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            this.store.addRolePlayer(conceptMock);
        }

        int orphanAttributes = this.store.totalOrphanAttributes();
        assertEquals(orphanAttributes, 1);
    }

    @Test
    public void whenRelationshipsDoNotOverlap_overlapEmpty() {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // add all but the relationship (last element)
        for (int i = 0 ; i < conceptMocks.size()-1; i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            this.store.addRolePlayer(conceptMock);
        }

        int relationshipDoubleCounts = this.store.totalRelationshipsRolePlayersOverlap();
        assertEquals(relationshipDoubleCounts, 0);
    }

    @Test
    public void whenRelationshipsDoNotOverlap_overlapOne() {
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);

        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // add all as role players
        for (int i = 0 ; i < conceptMocks.size(); i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            this.store.addRolePlayer(conceptMock);
        }

        int relationshipDoubleCounts = this.store.totalRelationshipsRolePlayersOverlap();
        assertEquals(relationshipDoubleCounts, 1);
    }
}