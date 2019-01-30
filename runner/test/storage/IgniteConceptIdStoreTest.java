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
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 *
 */
public class IgniteConceptIdStoreTest {

    private IgniteConceptIdStore store;
    private HashSet<String> typeLabelsSet;
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
        relTypeLabel = "friendship";
        typeLabelsSet.add(relTypeLabel);
        relationshipTypes = new HashSet<>();
        RelationshipType friendRelationshipType = mock(RelationshipType.class);
        when(friendRelationshipType.label()).thenReturn(Label.of("friendship"));
        relationshipTypes.add(friendRelationshipType);

        Concept relConceptMock = mock(Concept.class);
        conceptMocks.add(relConceptMock);
        Thing relThingMock = mock(Thing.class);
        when(relConceptMock.asThing()).thenReturn(relThingMock); // Thing
        when(relThingMock.id()).thenReturn(ConceptId.of("V9")); // Concept Id
        conceptIds.add(relThingMock.id());
        Type relConceptTypeMock = mock(Type.class);
        when(relThingMock.type()).thenReturn(relConceptTypeMock); // Concept Type
        when(relConceptTypeMock.label()).thenReturn(Label.of("friendship")); // Type label

        // create new ignite store
        this.store = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);
    }

    @Test
    public void whenConceptIdsAreAdded_conceptIdsAreInTheDB() throws SQLException {
        // Add all of the elements
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        int counter = 0;
        // Check objects were added to the db
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + this.entityTypeLabel + "_entity")) {
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
    public void whenConceptIsAdded_conceptIdCanBeRetrieved()  {
        int index = 0;
        this.store.addConcept(this.conceptMocks.get(index));
        ConceptId personConceptId = this.store.getConceptId(this.entityTypeLabel, index);
        System.out.println("Found id: " + personConceptId.toString());
        assertEquals(personConceptId, this.conceptIds.get(index));
    }

    @Test
    public void whenGettingIdWithOffset_correctIdIsReturned() {
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
    public void whenCountingTypeInstances_resultIsCorrect() {
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        int count = this.store.getConceptCount(this.entityTypeLabel);
        assertEquals(7, count);
    }

    @Test
    public void whenAllButOnePlayingRole_orphanEntitiesCorrect() {
        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // add 6 of 7 entities as role players too
        for (int i = 0 ; i < 6; i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            Thing thing = conceptMock.asThing();
            this.store.addRolePlayer(thing.id().toString(), thing.type().label().toString(), relTypeLabel, "somerole");
        }

        int orphanEntities = this.store.totalOrphanEntities();
        assertEquals(1, orphanEntities);
    }

    @Test
    public void whenAllButOnePlayingRole_orphanAttributesCorrect() {
        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // ad all but the attribute and relationship
        for (int i = 0 ; i < conceptMocks.size()-2; i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            Thing thing = conceptMock.asThing();
            this.store.addRolePlayer(thing.id().toString(), thing.type().label().toString(), relTypeLabel,"somerole");
        }

        int orphanAttributes = this.store.totalOrphanAttributes();
        assertEquals(1, orphanAttributes);
    }

    @Test
    public void whenRelationshipsDoNotOverlap_overlapEmpty() {
        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // add all but the relationship (last element)
        for (int i = 0 ; i < conceptMocks.size()-1; i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            Thing thing = conceptMock.asThing();
            this.store.addRolePlayer(thing.id().toString(), thing.type().label().toString(), relTypeLabel, "somerole");
        }

        int relationshipDoubleCounts = this.store.totalRelationshipsRolePlayersOverlap();
        assertEquals(0, relationshipDoubleCounts);
    }

    @Test
    public void whenRelationshipPlaysRole_overlapOne() {
        // add all concepts to store
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        // add all as role players
        for (int i = 0 ; i < conceptMocks.size(); i++) {
            Concept conceptMock = this.conceptMocks.get(i);
            Thing thing = conceptMock.asThing();
            this.store.addRolePlayer(thing.id().toString(), thing.type().label().toString(), relTypeLabel, "somerole");
        }

        int relationshipDoubleCounts = this.store.totalRelationshipsRolePlayersOverlap();
        assertEquals(1, relationshipDoubleCounts);
    }

    @Test
    public void whenEntitiesDoNotPlayRoles_allEntitiesReturned() {
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }
        String typeLabel = "person"; // we have 7 mocked people
        List<String> peopleNotPlayingRoles = this.store.getIdsNotPlayingRole(typeLabel, relTypeLabel, "aRole");
        assertEquals(7, peopleNotPlayingRoles.size());

    }

    @Test
    public void whenEntityPlaysSpecificRole_notReturnedWhenAskingForEntitiesNotPlayingRole() {
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }
        Concept aPerson = this.conceptMocks.get(0);
        String personTypeLabel = aPerson.asThing().type().label().toString(); // follow what's implemented in mocks
        String relationshipType = relationshipTypes.stream().findFirst().get().label().toString();
        String role = "some-role"; // test the string safety conversion too by including -

        this.store.addRolePlayer(aPerson.asThing().id().toString(), personTypeLabel, relationshipType, role);

        List<String> entitiesNotPlayingRole = store.getIdsNotPlayingRole(personTypeLabel, relationshipType, role);
        assertEquals(6, entitiesNotPlayingRole.size());
    }

    @Test
    public void whenEntityPlaysSpecificTwoRoles_notReturnedWhenAskingForEntitiesNotPlayingEitherRole() {
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }
        Concept aPerson = this.conceptMocks.get(0);
        String personTypeLabel = aPerson.asThing().type().label().toString(); // follow what's implemented in mocks
        String relationshipType = relationshipTypes.stream().findFirst().get().label().toString();
        String role1 = "some-role-1"; // test the string safety conversion too by including -
        String role2 = "some-role-2"; // test the string safety conversion too by including -

        this.store.addRolePlayer(aPerson.asThing().id().toString(), personTypeLabel, relationshipType, role1);
        this.store.addRolePlayer(aPerson.asThing().id().toString(), personTypeLabel, relationshipType, role2);

        List<String> entitiesNotPlayingRole1 = store.getIdsNotPlayingRole(personTypeLabel, relationshipType, role1);
        List<String> entitiesNotPlayingRole2 = store.getIdsNotPlayingRole(personTypeLabel, relationshipType, role2);

        String[] correctEntities = this.conceptMocks.subList(1, 7).stream()
                .map(concept->concept.asThing().id().toString())
                .collect(Collectors.toList()).toArray(new String[]{});

        // assert matches in any order, casting to force hamcrest to use the right
        assertThat(entitiesNotPlayingRole1, containsInAnyOrder(correctEntities));
        assertThat(entitiesNotPlayingRole2, containsInAnyOrder(correctEntities));
    }


    @Test
    public void whenEntityPlaysRole_countIsCorrect() {
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }
        Concept aPerson = this.conceptMocks.get(0);
        String personTypeLabel = aPerson.asThing().type().label().toString(); // follow what's implemented in mocks
        String relationshipType = relationshipTypes.stream().findFirst().get().label().toString();
        String role = "some-role"; // test the string safety conversion too by including -

        this.store.addRolePlayer(aPerson.asThing().id().toString(), personTypeLabel, relationshipType, role);

        int entitiesNotPlayingRole = this.store.numIdsNotPlayingRole(personTypeLabel, relationshipType, role);
        assertEquals(6, entitiesNotPlayingRole);
    }

    @Test
    public void whenAttributePlaysNoRole_orphanCountIsCorrect() {
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }
        int orphanAttributes = this.store.totalOrphanAttributes();
        assertEquals(1, orphanAttributes);
    }

    @Test
    public void whenAttributePlaysRole_orphanCountIsCorrect() {
        for (Concept conceptMock : this.conceptMocks) {
            this.store.addConcept(conceptMock);
        }

        Concept anAge = this.conceptMocks.get(7);
        String ageId = anAge.asThing().id().toString();
        String ageLabel = anAge.asThing().type().label().toString();
        this.store.addRolePlayer(ageId, ageLabel, "@has-" + ageLabel, "@has-"+ageLabel+"-value");

        int orphanAttributes = this.store.totalOrphanAttributes();
        assertEquals(0, orphanAttributes);
    }

}