/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016-2019 Grakn Labs Limited
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

package grakn.benchmark.generator.storage;

import grakn.benchmark.generator.util.KeyspaceSchemaLabels;
import grakn.core.concept.Concept;
import grakn.core.concept.ConceptId;
import grakn.core.concept.Label;
import grakn.core.concept.thing.Attribute;
import grakn.core.concept.thing.Thing;
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Type;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class IgniteConceptStorageTest {

    private IgniteConceptStorage store;
    private HashSet<String> typeLabelsSet;
    private HashMap<ConceptId, Long> conceptIdKeys;
    private HashMap<Concept, Long> conceptMockKeys;
    private String entityTypeLabel;
    HashSet<String> entityTypes;

    private String attrTypeLabel;
    Map<String, AttributeType.DataType<?>> attributeTypes;

    private String relTypeLabel;
    HashSet<String> relationshipTypes;

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
        entityTypes.add("person");

        conceptIdKeys = new HashMap<>();
        conceptIdKeys.put(ConceptId.of("V123456"), 1L);
        conceptIdKeys.put(ConceptId.of("V298345"), 2L);
        conceptIdKeys.put(ConceptId.of("V380325"), 3L);
        conceptIdKeys.put(ConceptId.of("V4"), 4L);
        conceptIdKeys.put(ConceptId.of("V5"), 5L);
        conceptIdKeys.put(ConceptId.of("V6"), 6L);
        conceptIdKeys.put(ConceptId.of("V7"), 7L);

        conceptMockKeys = new HashMap<>();

        Iterator<ConceptId> idIterator = conceptIdKeys.keySet().iterator();

        while (idIterator.hasNext()) {
            ConceptId conceptId = idIterator.next();
            Long conceptKey = conceptIdKeys.get(conceptId);

            // Concept
            Concept conceptMock = mock(Concept.class);
            conceptMockKeys.put(conceptMock, conceptKey);

            // Thing
            Thing thingMock = mock(Thing.class);
            when(conceptMock.asThing()).thenReturn(thingMock);

            // ConceptID

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
        attributeTypes = new HashMap<>();
        AttributeType ageAttributeType = mock(AttributeType.class);
        when(ageAttributeType.label()).thenReturn(Label.of(attrTypeLabel));
        when(ageAttributeType.dataType()).thenReturn(AttributeType.DataType.LONG); // Data Type
        attributeTypes.put(attrTypeLabel, AttributeType.DataType.LONG);

        Concept conceptMock = mock(Concept.class);
        conceptMockKeys.put(conceptMock, 8L);
        Thing thingMock = mock(Thing.class);
        when(conceptMock.asThing()).thenReturn(thingMock); // Thing
        when(thingMock.id()).thenReturn(ConceptId.of("V8")); // Concept Id
        conceptIdKeys.put(thingMock.id(), 8L);
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
        RelationType friendRelationshipType = mock(RelationType.class);
        when(friendRelationshipType.label()).thenReturn(Label.of("friendship"));
        relationshipTypes.add(relTypeLabel);

        Concept relConceptMock = mock(Concept.class);
        conceptMockKeys.put(relConceptMock, 9L);
        Thing relThingMock = mock(Thing.class);
        when(relConceptMock.asThing()).thenReturn(relThingMock); // Thing
        when(relThingMock.id()).thenReturn(ConceptId.of("V9")); // Concept Id
        conceptIdKeys.put(relThingMock.id(), 9L);
        Type relConceptTypeMock = mock(Type.class);
        when(relThingMock.type()).thenReturn(relConceptTypeMock); // Concept Type
        when(relConceptTypeMock.label()).thenReturn(Label.of("friendship")); // Type label
        KeyspaceSchemaLabels schemaLabels = mock(KeyspaceSchemaLabels.class);
        when(schemaLabels.attributeLabelsDataTypes()).thenReturn(attributeTypes);
        when(schemaLabels.entityLabels()).thenReturn(entityTypes);
        when(schemaLabels.relationLabels()).thenReturn(relationshipTypes);
        // create new ignite store
        store = new IgniteConceptStorage(schemaLabels);
    }

    @Test
    public void whenConceptIdsAreAdded_conceptIdsAreInTheDB() throws SQLException {
        // Add all of the elements
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        int counter = 0;
        // Check objects were added to the db
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + entityTypeLabel + "_entity")) {
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
    public void whenConceptIsAdded_conceptKeyCanBeRetrieved() {
        Concept aConcept = conceptMockKeys.keySet().stream().filter(concept -> concept.asThing().type().label().toString().equals(entityTypeLabel)).findFirst().get();
        store.addConcept(aConcept, conceptMockKeys.get(aConcept));
        Long aConceptKey = store.getConceptKey(entityTypeLabel, 0); // get 0th offset
        System.out.println("Found key: " + aConceptKey.toString());
        assertEquals(aConceptKey, conceptMockKeys.get(aConcept));
    }

    @Test
    public void whenGettingIdWithOffset_correctKeyIsReturned() {
        int index = 4;
        // Add all of the elements

        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        long personConceptKey = store.getConceptKey(entityTypeLabel, index);
        System.out.println("Found key: " + personConceptKey);
        assertEquals(5L, personConceptKey);
    }

    @Test
    public void whenCountingTypeInstances_resultIsCorrect() {
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        int count = store.getConceptCount(entityTypeLabel);
        assertEquals(7, count);
    }

    @Test
    public void whenAllButOnePlayingRole_orphanEntitiesCorrect() {
        // add all concepts to store
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        // add 6 of 7 entities as role players too
        Iterator<Concept> iterator = conceptMockKeys.keySet().iterator();
        conceptMockKeys.keySet().stream()
                .filter(concept -> concept.asThing().type().label().toString().equals(entityTypeLabel))
                .limit(6)
                .forEach(conceptMock -> {
                    Thing thing = conceptMock.asThing();
                    store.addRolePlayerByKey(conceptMockKeys.get(conceptMock), thing.type().label().toString(), relTypeLabel, "somerole");
                });

        int orphanEntities = store.totalOrphanEntities();
        assertEquals(1, orphanEntities);
    }

    @Test
    public void whenAllButOnePlayingRole_orphanAttributesCorrect() {
        // add all concepts to store
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        for (Concept conceptMock : conceptMockKeys.keySet()) {
            String conceptType = conceptMock.asThing().type().label().toString();
            if (relationshipTypes.contains(conceptType) || attributeTypes.containsKey(conceptType)) {
                // add all but the attribute and relationship, so skip these
                continue;
            }
            Thing thing = conceptMock.asThing();
            store.addRolePlayerByKey(conceptMockKeys.get(conceptMock), thing.type().label().toString(), relTypeLabel, "somerole");
        }

        int orphanAttributes = store.totalOrphanAttributes();
        assertEquals(1, orphanAttributes);
    }

    @Test
    public void whenRelationshipsDoNotOverlap_overlapEmpty() {
        // add all concepts to store
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        for (Concept conceptMock : conceptMockKeys.keySet()) {
            if (relationshipTypes.contains(conceptMock.asThing().type().label().toString())) {
                // add all but the relation concept, so skip these
                continue;
            }
            Thing thing = conceptMock.asThing();
            store.addRolePlayerByKey(conceptMockKeys.get(conceptMock), thing.type().label().toString(), relTypeLabel, "somerole");
        }

        int relationshipDoubleCounts = store.totalRelationshipsRolePlayersOverlap();
        assertEquals(0, relationshipDoubleCounts);
    }

    @Test
    public void whenRelationshipPlaysRole_overlapOne() {
        // add all concepts to store
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        // add all as role players
        Iterator<Concept> iterator = conceptMockKeys.keySet().iterator();
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            Thing thing = conceptMock.asThing();
            store.addRolePlayerByKey(conceptMockKeys.get(conceptMock), thing.type().label().toString(), relTypeLabel, "somerole");
        }

        int relationshipDoubleCounts = store.totalRelationshipsRolePlayersOverlap();
        assertEquals(1, relationshipDoubleCounts);
    }

    @Test
    public void whenEntitiesDoNotPlayRoles_allEntitiesReturned() {
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }
        String typeLabel = "person"; // we have 7 mocked people
        List<Long> peopleNotPlayingRoles = store.getKeysNotPlayingRole(typeLabel, relTypeLabel, "aRole");
        assertEquals(7, peopleNotPlayingRoles.size());

    }

    @Test
    public void whenEntityPlaysSpecificRole_notReturnedWhenAskingForEntitiesNotPlayingRole() {
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }
        Concept aPerson = conceptMockKeys.keySet().stream().filter(concept -> concept.asThing().type().label().toString().equals("person")).findFirst().get();
        String personTypeLabel = aPerson.asThing().type().label().toString(); // follow what's implemented in mocks
        String relationshipType = relationshipTypes.stream().findFirst().get();
        String role = "some-role"; // test the string safety conversion too by including -

        store.addRolePlayerByKey(conceptMockKeys.get(aPerson), personTypeLabel, relationshipType, role);

        List<Long> entitiesNotPlayingRole = store.getKeysNotPlayingRole(personTypeLabel, relationshipType, role);
        assertEquals(6, entitiesNotPlayingRole.size());
    }

    @Test
    public void whenEntityPlaysSpecificTwoRoles_notReturnedWhenAskingForEntitiesNotPlayingEitherRole() {
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }
        Concept aPerson = conceptMockKeys.keySet().stream().filter(concept -> concept.asThing().type().label().toString().equals("person")).findFirst().get();
        String personTypeLabel = aPerson.asThing().type().label().toString(); // follow what's implemented in mocks
        String relationshipType = relationshipTypes.stream().findFirst().get();
        String role1 = "some-role-1"; // test the string safety conversion too by including -
        String role2 = "some-role-2"; // test the string safety conversion too by including -

        store.addRolePlayerByKey(conceptMockKeys.get(aPerson), personTypeLabel, relationshipType, role1);
        store.addRolePlayerByKey(conceptMockKeys.get(aPerson), personTypeLabel, relationshipType, role2);

        List<Long> entitiesNotPlayingRole1 = store.getKeysNotPlayingRole(personTypeLabel, relationshipType, role1);
        List<Long> entitiesNotPlayingRole2 = store.getKeysNotPlayingRole(personTypeLabel, relationshipType, role2);

        Long[] correctEntities = conceptMockKeys.entrySet().stream()
                .filter(entry -> entry.getKey().asThing().type().label().toString().equals("person") && !entry.getKey().asThing().id().equals(aPerson.asThing().id()))
                .map(entry -> entry.getValue())
                .collect(Collectors.toList()).toArray(new Long[]{});

        // assert matches in any order, casting to force hamcrest to use the right
        assertThat(entitiesNotPlayingRole1, containsInAnyOrder(correctEntities));
        assertThat(entitiesNotPlayingRole2, containsInAnyOrder(correctEntities));
    }


    @Test
    public void whenEntityPlaysRole_countIsCorrect() {
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }
        Concept aPerson = conceptMockKeys.keySet().stream().filter(concept -> concept.asThing().type().label().toString().equals("person")).findFirst().get();
        String personTypeLabel = aPerson.asThing().type().label().toString(); // follow what's implemented in mocks
        String relationshipType = relationshipTypes.stream().findFirst().get();
        String role = "some-role"; // test the string safety conversion too by including -

        store.addRolePlayerByKey(conceptMockKeys.get(aPerson), personTypeLabel, relationshipType, role);

        int entitiesNotPlayingRole = store.numIdsNotPlayingRole(personTypeLabel, relationshipType, role);
        assertEquals(6, entitiesNotPlayingRole);
    }

    @Test
    public void whenAttributePlaysNoRole_orphanCountIsCorrect() {
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }
        int orphanAttributes = store.totalOrphanAttributes();
        assertEquals(1, orphanAttributes);
    }

    @Test
    public void whenAttributePlaysRole_orphanCountIsCorrect() {
        for (Concept conceptMock : conceptMockKeys.keySet()) {
            store.addConcept(conceptMock, conceptMockKeys.get(conceptMock));
        }

        Concept anAge = conceptMockKeys.keySet().stream().filter(concept -> concept.asThing().type().label().toString().equals("age")).findFirst().get();
        String ageLabel = anAge.asThing().type().label().toString();
        store.addRolePlayerByKey(conceptMockKeys.get(anAge), ageLabel, "@has-" + ageLabel, "@has-" + ageLabel + "-value");

        int orphanAttributes = store.totalOrphanAttributes();
        assertEquals(0, orphanAttributes);
    }

}
