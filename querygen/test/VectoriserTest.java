package grakn.benchmark.querygen;

import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.EntityType;
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Role;
import grakn.core.concept.type.Type;
import graql.lang.statement.Variable;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VectoriserTest {

    @Test
    public void numVariablesTest() {
        QueryBuilder builder = new QueryBuilder();
        Variable v = builder.reserveNewVariable();
        Type t = Mockito.mock(Type.class);
        builder.addMapping(v, t);

        Assert.assertEquals(Vectoriser.numVariables(builder), 1);
    }

    @Test
    public void testMeanRolesPerRelation() {
        QueryBuilder builder = new QueryBuilder();
        // create one relation variable
        Variable v = builder.reserveNewVariable();
        RelationType relationType = Mockito.mock(RelationType.class);
        when(relationType.isRelationType()).thenReturn(true);
        builder.addMapping(v, relationType);

        // with two role player
        Variable rolePlayerVar = builder.reserveNewVariable();
        Role r1 = Mockito.mock(Role.class);
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
        RelationType relationType = Mockito.mock(RelationType.class);
        when(relationType.isRelationType()).thenReturn(true);
        builder.addMapping(v, relationType);

        // with two role player
        Variable rolePlayerVar = builder.reserveNewVariable();
        Role r1 = Mockito.mock(Role.class);
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
        // create one relation variable

        Variable v1 = builder.reserveNewVariable();
        // an entity type that can have 2 different attribute types
        EntityType entityType = Mockito.mock(EntityType.class);
        AttributeType attributeType = Mockito.mock(AttributeType.class);
        AttributeType attributeType2 = Mockito.mock(AttributeType.class);
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
        EntityType entityType2 = Mockito.mock(EntityType.class);
        when(entityType2.attributes()).thenReturn(Stream.empty());
        builder.addMapping(v2, entityType2);

        assertEquals(3.0/1.0, Vectoriser.meanAttributesOwnedPerThing(builder), 0.0001);
    }
}
