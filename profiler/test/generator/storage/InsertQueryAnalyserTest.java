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

package grakn.benchmark.profiler.generator.storage;

import grakn.benchmark.profiler.generator.DataGeneratorException;
import grakn.benchmark.profiler.generator.util.InsertQueryAnalyser;
import grakn.core.concept.Concept;
import grakn.core.concept.ConceptId;
import grakn.core.concept.Thing;
import grakn.core.graql.Graql;
import grakn.core.graql.InsertQuery;
import grakn.core.graql.Var;
import grakn.core.graql.VarPattern;
import grakn.core.graql.answer.ConceptMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static grakn.core.graql.Graql.var;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 *
 */
public class InsertQueryAnalyserTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private ArrayList<ConceptMap> mockConceptMaps(Map<Var, String> variables) {

        ArrayList<ConceptMap> answerList = new ArrayList<>();
        ConceptMap answerMock = mock(ConceptMap.class);
        for (Map.Entry<Var, String> variable : variables.entrySet()) {

            // Mock the answer object
            Concept conceptMock = mock(Concept.class);
            Thing thingMock = mock(Thing.class);
            when(conceptMock.asThing()).thenReturn(thingMock);
            when(thingMock.id()).thenReturn(ConceptId.of(variable.getValue()));
            when(answerMock.get(variable.getKey())).thenReturn(conceptMock);

        }
        answerList.add(answerMock);
        return answerList;
    }

    @Test
    public void whenEntityInserted_identifyEntityWasInserted() {

        Var x = var("x");
        InsertQuery query = Graql.insert(x.isa("company"));

        HashMap<Var, String> vars = new HashMap<>();
        vars.put(x, "V123456");
        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals("V123456", insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenRelationshipInserted_identifyRelationshipWasInserted() {

        String rId = "Vr";
        String xId = "Vx";
        String yId = "Vy";
        String zId = "Vz";

        Var r = var("r");
        Var x = var("x").asUserDefined();
        Var y = var("y").asUserDefined();
        Var z = var("z").asUserDefined();

        HashMap<Var, String> vars = new HashMap<>();
        vars.put(r, rId);
        vars.put(x, xId);
        vars.put(y, yId);
        vars.put(z, zId);

        InsertQuery query = Graql.match(
                x.id(ConceptId.of(xId))
                        .and(y.id(ConceptId.of(xId)))
                        .and(z.id(ConceptId.of(xId)))
        ).insert(
                r.isa("employment")
                        .rel("employee", x)
                        .rel("employee", y)
                        .rel("employee", z));

        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals(rId, insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenRelationshipInsertedWithIdsInInsert_identifyRelationshipWasInserted() {

        String rId = "Vr";
        String xId = "Vx";
        String yId = "Vy";
        String zId = "Vz";

        Var r = var("r");
        Var x = var("x").asUserDefined();
        Var y = var("y").asUserDefined();
        Var z = var("z").asUserDefined();

        HashMap<Var, String> vars = new HashMap<>();
        vars.put(r, rId);
        vars.put(x, xId);
        vars.put(y, yId);
        vars.put(z, zId);

        InsertQuery query = Graql.insert(
                r.isa("employment")
                        .rel("employee", x)
                        .rel("employee", y)
                        .rel("employee", z),
                        x.id(ConceptId.of(xId)),
                        y.id(ConceptId.of(xId)),
                        z.id(ConceptId.of(xId)));

        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals(rId, insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenAttributeInserted_identifyAttributeWasInserted() {
//        varPatternAdmin.commonVars()

        String xId = "Vx";
        String yId = "Vy";

        String cAttr = "c-name";

        Var x = var("x").asUserDefined();
        Var y = var("y").asUserDefined();

        HashMap<Var, String> vars = new HashMap<>();
        vars.put(x, xId);
        vars.put(y, yId);

//        InsertQuery query = Graql.insert(x.isa("company").has("name", cAttr));
        InsertQuery query = Graql.insert(x.isa("company").has("name", y), x.id(ConceptId.of(xId)), y.val(cAttr));

        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals(yId, insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenAttributeInsertedWithIdInClause_identifyAttributeWasInserted() {
//        varPatternAdmin.commonVars()

        String xId = "Vx";
        String yId = "Vy";

        String cAttr = "c-name";

        Var x = var("x").asUserDefined();
        Var y = var("y").asUserDefined();

        HashMap<Var, String> vars = new HashMap<>();
        vars.put(x, xId);
        vars.put(y, yId);

//        InsertQuery query = Graql.insert(x.isa("company").has("name", cAttr));
        InsertQuery query = Graql.insert(x.isa("company").has("name", y).id(ConceptId.of(xId)), y.val(cAttr));

        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals(yId, insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenInsertRelationship_identifyRolePlayers() {
        VarPattern x = var("x").asUserDefined().id(ConceptId.of("V123"));
        VarPattern y = var("y").asUserDefined().id(ConceptId.of("V234"));
        InsertQuery insertQuery = Graql.match(x, y).insert(var("r").rel("friend", x).rel("friend", y).isa("friendship"));

        ConceptMap map = mock(ConceptMap.class);
        Concept xConcept = mock(Concept.class);
        when(xConcept.id()).thenReturn(ConceptId.of("V123"));
        Concept yConcept = mock(Concept.class);
        when(yConcept.id()).thenReturn(ConceptId.of("V234"));
        when(map.get(var("x"))).thenReturn(xConcept);
        when(map.get(var("y"))).thenReturn(yConcept);

        Map<Concept, String> rolePlayers = InsertQueryAnalyser.getRolePlayersAndRoles(insertQuery, Arrays.asList(map));
        assertTrue(rolePlayers.containsKey(xConcept));
        assertEquals("friend", rolePlayers.get(xConcept));
        assertTrue(rolePlayers.containsKey(yConcept));
        assertEquals("friend", rolePlayers.get(yConcept));
        assertEquals(2, rolePlayers.size());
    }

    @Test
    public void whenInsertNonRelationship_returnEmptySet() {
        VarPattern x = var("x").asUserDefined();
        VarPattern y = var("y").asUserDefined();
        InsertQuery insertQuery = Graql.insert(x.isa("company").has("name", y).id(ConceptId.of("V123")), y.val("john"));

        ConceptMap map = mock(ConceptMap.class);
        Concept xConcept = mock(Concept.class);
        when(xConcept.id()).thenReturn(ConceptId.of("V123"));
        Concept yConcept = mock(Concept.class);
        when(yConcept.id()).thenReturn(ConceptId.of("V234"));
        when(map.get(var("x"))).thenReturn(xConcept);
        when(map.get(var("y"))).thenReturn(yConcept);

        Map<Concept, String> rolePlayers = InsertQueryAnalyser.getRolePlayersAndRoles(insertQuery, Arrays.asList(map));
        assertEquals(0, rolePlayers.size());
    }

    @Test
    public void whenInsertWithoutRole_throwException() {
        VarPattern x = var("x").asUserDefined().id(ConceptId.of("V123"));
        VarPattern y = var("y").asUserDefined().id(ConceptId.of("V234"));
        InsertQuery insertQuery = Graql.match(x, y).insert(var("r").rel( x).rel(y).isa("friendship"));

        expectedException.expect(DataGeneratorException.class);
        expectedException.expectMessage("Require explicit roles in data generator");
        InsertQueryAnalyser.getRolePlayersAndRoles(insertQuery, null);
    }

    @Test
    public void whenRelationshipInserted_relationshipLabelFound() {
        VarPattern x = var("x").asUserDefined().id(ConceptId.of("V123"));
        VarPattern y = var("y").asUserDefined().id(ConceptId.of("V234"));
        InsertQuery insertQuery = Graql.match(x, y).insert(var("r").rel("friend", x).rel("friend", y).isa("friendship"));
        String relationshipLabel = InsertQueryAnalyser.getRelationshipTypeLabel(insertQuery);
        assertEquals("friendship", relationshipLabel);
    }

    @Test
    public void whenNoRelationshipInserted_nullReturned() {
        VarPattern x = var("x").asUserDefined();
        VarPattern y = var("y").asUserDefined();
        InsertQuery insertQuery = Graql.insert(x.isa("company").has("name", y).id(ConceptId.of("V123")), y.val("john"));
        String relationshipLabel = InsertQueryAnalyser.getRelationshipTypeLabel(insertQuery);
        assertEquals(null, relationshipLabel);
    }

}