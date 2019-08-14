package grakn.benchmark.querygen;

import grakn.core.concept.Label;
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.EntityType;
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Role;
import grakn.core.concept.type.Type;
import graql.lang.statement.Variable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VectoriserTest {

    @Test
    public void numVariablesTest() {
        QueryBuilder builder = new QueryBuilder();
        Variable v = builder.reserveNewVariable();
        Type t = mock(Type.class);
        builder.addMapping(v, t);

        Assert.assertEquals(Vectoriser.numVariables(builder), 1);
    }

    @Test
    public void testMeanRolesPerRelation() {
        QueryBuilder builder = new QueryBuilder();
        // create one relation variable
        Variable v = builder.reserveNewVariable();
        RelationType relationType = mock(RelationType.class);
        when(relationType.isRelationType()).thenReturn(true);
        builder.addMapping(v, relationType);

        // with two role player
        Variable rolePlayerVar = builder.reserveNewVariable();
        Role r1 = mock(Role.class);
        builder.addRolePlayer(v, rolePlayerVar, r1);
        Variable anotherRolePlayerVar = builder.reserveNewVariable();
        builder.addRolePlayer(v, anotherRolePlayerVar, r1);

        assertEquals(2.0, Vectoriser.meanRolesPerRelation(builder), 0.0001);

        Variable anotherRelation = builder.reserveNewVariable();
        RelationType anotherRelationType = mock(RelationType.class);
        when(anotherRelationType.isRelationType()).thenReturn(true);
        builder.addMapping(anotherRelation, anotherRelationType);

        assertEquals(1.0, Vectoriser.meanRolesPerRelation(builder), 00001);
    }

    @Test
    public void testMeanUniqueRolesPerRelation() {
        QueryBuilder builder = new QueryBuilder();
        // create one relation variable
        Variable v = builder.reserveNewVariable();
        RelationType relationType = mock(RelationType.class);
        when(relationType.isRelationType()).thenReturn(true);
        builder.addMapping(v, relationType);

        // with two role player
        Variable rolePlayerVar = builder.reserveNewVariable();
        Role r1 = mock(Role.class);
        builder.addRolePlayer(v, rolePlayerVar, r1);
        Variable anotherRolePlayerVar = builder.reserveNewVariable();
        builder.addRolePlayer(v, anotherRolePlayerVar, r1);

        assertEquals(1.0, Vectoriser.meanUniqueRolesPerRelation(builder), 0.0001);

        Variable anotherRelation = builder.reserveNewVariable();
        RelationType anotherRelationType = mock(RelationType.class);
        when(anotherRelationType.isRelationType()).thenReturn(true);
        builder.addMapping(anotherRelation, anotherRelationType);

        assertEquals(0.5, Vectoriser.meanUniqueRolesPerRelation(builder), 0.0001);
    }


    @Test
    public void testMeanAttributesOwnedPerThing() {
        QueryBuilder builder = new QueryBuilder();

        Variable v1 = builder.reserveNewVariable();
        // an entity type that can have 2 different attribute types
        EntityType entityType = mock(EntityType.class);
        AttributeType attributeType = mock(AttributeType.class);
        AttributeType attributeType2 = mock(AttributeType.class);
        when(entityType.attributes()).thenReturn(Stream.of(attributeType, attributeType2));
        builder.addMapping(v1, entityType);

        // the variable actually owns 3, with one of them being repeated twice
        Variable a1 = builder.reserveNewVariable();
        builder.addMapping(a1, attributeType);
        builder.addOwnership(v1, a1);
        Variable a2 = builder.reserveNewVariable();
        builder.addMapping(a2, attributeType);
        builder.addOwnership(v1, a2);
        Variable a3 = builder.reserveNewVariable();
        builder.addMapping(a3, attributeType2);
        builder.addOwnership(v1, a3);

        Variable v2 = builder.reserveNewVariable();
        // an entity type that can have 0 attribute types
        EntityType entityType2 = mock(EntityType.class);
        when(entityType2.attributes()).thenReturn(Stream.empty());
        builder.addMapping(v2, entityType2);

        assertEquals(3.0 / 1.0, Vectoriser.meanAttributesOwnedPerThing(builder), 0.0001);
    }

    /**
     * Query:
     * e1 --> a1, a2
     * | \
     * r1 / a3
     * |
     * e2
     * <p>
     * 4! + 3! + 2! + 3*1! = 35
     */
    @Test
    public void testAmbiguity() {
        QueryBuilder builder = new QueryBuilder();

        Variable e1 = builder.reserveNewVariable();
        EntityType entityType = mock(EntityType.class);
        AttributeType attributeType = mock(AttributeType.class);
        AttributeType attributeType2 = mock(AttributeType.class);
        builder.addMapping(e1, entityType);

        Variable a1 = builder.reserveNewVariable();
        builder.addMapping(a1, attributeType);
        builder.addOwnership(e1, a1);
        Variable a2 = builder.reserveNewVariable();
        builder.addMapping(a2, attributeType);
        builder.addOwnership(e1, a2);
        Variable a3 = builder.reserveNewVariable();
        builder.addMapping(a3, attributeType2);
        builder.addOwnership(e1, a3);

        Variable e2 = builder.reserveNewVariable();
        EntityType entityType2 = mock(EntityType.class);
        builder.addMapping(e2, entityType2);

        Variable r1 = builder.reserveNewVariable();
        RelationType relationType = mock(RelationType.class);
        builder.addMapping(r1, relationType);
        builder.addOwnership(r1, a3);

        Role role1 = mock(Role.class);
        Role role2 = mock(Role.class);

        builder.addRolePlayer(r1, e1, role1);
        builder.addRolePlayer(r1, e2, role2);

        assertEquals(35.0, Vectoriser.ambiguity(builder), 0.0001);
    }

    @Test
    public void testSpecificity() {
        QueryBuilder builder = new QueryBuilder();

        // first, build up the whole type hierarchy
        // thing - entity - person
        // thing - relation [@has-attribute - @has-name, marriage]
        // thing - attribute

        Type thing = mock(Type.class);
        when(thing.label()).thenReturn(Label.of("thing"));
        EntityType entity = mock(EntityType.class);
        when(entity.label()).thenReturn(Label.of("entity"));
        EntityType person = mock(EntityType.class);
        when(person.label()).thenReturn(Label.of("person"));
        RelationType relation = mock(RelationType.class);
        when(relation.label()).thenReturn(Label.of("relation"));
        RelationType hasAttribute = mock(RelationType.class);
        when(hasAttribute.label()).thenReturn(Label.of("@has-attribute"));
        RelationType hasName = mock(RelationType.class);
        when(hasName.label()).thenReturn(Label.of("@has-name"));
        RelationType marriage = mock(RelationType.class);
        when(marriage.label()).thenReturn(Label.of("marriage"));
        AttributeType attribute = mock(AttributeType.class);
        when(attribute.label()).thenReturn(Label.of("attribute"));

        // first inheritance layer returns null
        when(entity.sup()).thenReturn(null);
        when(relation.sup()).thenReturn(null);
        when(attribute.sup()).thenReturn(null);

        // second inheritance layer
        when(person.sup()).thenReturn(entity);
        when(hasAttribute.sup()).thenReturn(relation);
        when(marriage.sup()).thenReturn(relation);

        // third inheritance layer
        when(hasName.sup()).thenReturn(hasAttribute);

        // downwards subs() pointers
        Stream<Type> subs = Stream.of(thing, entity, person, relation, hasAttribute, hasName, marriage, attribute);
        doAnswer(invocation -> Stream.of(thing, entity, person, relation, hasAttribute, hasName, marriage, attribute)).when(thing).subs(); // unchecked return
        doAnswer(invocation -> Stream.of(entity, person)).when(entity).subs();
        doAnswer(invocation -> Stream.of(person)).when(person).subs();
        doAnswer(invocation -> Stream.of(relation, hasAttribute, hasName, marriage)).when(relation).subs();
        doAnswer(invocation -> Stream.of(hasAttribute, hasName)).when(hasAttribute).subs();
        doAnswer(invocation -> Stream.of(hasName)).when(hasName).subs();
        doAnswer(invocation -> Stream.of(marriage)).when(marriage).subs();
        doAnswer(invocation -> Stream.of(attribute)).when(attribute).subs();


        // test depth calculation is correct
        assertEquals(0, Vectoriser.depth(thing));
        assertEquals(1, Vectoriser.depth(relation));
        assertEquals(2, Vectoriser.depth(hasAttribute));

        // test leafChildren is correct
        Set<Type> leafChildren = Vectoriser.leafChildren(thing);
        assertThat(leafChildren, containsInAnyOrder(person, marriage, hasName, attribute));

    }

    /**
     * Query:
     * e1 --> a1, a2
     * | \
     * r1 / a3
     * |
     * e2
     * <p>
     * 4! + 3! + 2! + 3*1! = 35
     */
    @Test
    public void testEdgesPerVariable() {
        QueryBuilder builder = new QueryBuilder();

        Variable e1 = builder.reserveNewVariable();
        EntityType entityType = mock(EntityType.class);
        AttributeType attributeType = mock(AttributeType.class);
        AttributeType attributeType2 = mock(AttributeType.class);
        builder.addMapping(e1, entityType);

        Variable a1 = builder.reserveNewVariable();
        builder.addMapping(a1, attributeType);
        builder.addOwnership(e1, a1);
        Variable a2 = builder.reserveNewVariable();
        builder.addMapping(a2, attributeType);
        builder.addOwnership(e1, a2);
        Variable a3 = builder.reserveNewVariable();
        builder.addMapping(a3, attributeType2);
        builder.addOwnership(e1, a3);

        Variable e2 = builder.reserveNewVariable();
        EntityType entityType2 = mock(EntityType.class);
        builder.addMapping(e2, entityType2);

        Variable r1 = builder.reserveNewVariable();
        RelationType relationType = mock(RelationType.class);
        builder.addMapping(r1, relationType);
        builder.addOwnership(r1, a3);

        Role role1 = mock(Role.class);
        Role role2 = mock(Role.class);

        builder.addRolePlayer(r1, e1, role1);
        builder.addRolePlayer(r1, e2, role2);

        assertEquals(2.0, Vectoriser.meanEdgesPerVariable(builder), 0.0001);

    }
}
