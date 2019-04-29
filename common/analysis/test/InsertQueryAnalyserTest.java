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

package grakn.benchmark.common.analysis;

import grakn.core.concept.Concept;
import grakn.core.concept.ConceptId;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.thing.Thing;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.Variable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static graql.lang.Graql.and;
import static graql.lang.Graql.var;
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

    private ArrayList<ConceptMap> mockConceptMaps(Map<Variable, String> variables) {

        ArrayList<ConceptMap> answerList = new ArrayList<>();
        ConceptMap answerMock = mock(ConceptMap.class);
        for (Map.Entry<Variable, String> variable : variables.entrySet()) {

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

        Variable x = new Variable("x");
        GraqlInsert query = Graql.insert(var(x).isa("company"));

        HashMap<Variable, String> vars = new HashMap<>();
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

        Variable r = new Variable("r");
        Variable x = new Variable("x").asReturnedVar();
        Variable y = new Variable("y").asReturnedVar();
        Variable z = new Variable("z").asReturnedVar();

        HashMap<Variable, String> vars = new HashMap<>();
        vars.put(r, rId);
        vars.put(x, xId);
        vars.put(y, yId);
        vars.put(z, zId);

        GraqlInsert query = Graql.match(
                and(var(x).id(xId),
                        var(y).id(xId),
                        var(z).id(xId))
        ).insert(var(r).isa("employment")
                .rel("employee", var(x))
                .rel("employee", var(y))
                .rel("employee", var(z)));

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

        Variable r = new Variable("r");
        Variable x = new Variable("x").asReturnedVar();
        Variable y = new Variable("y").asReturnedVar();
        Variable z = new Variable("z").asReturnedVar();

        HashMap<Variable, String> vars = new HashMap<>();
        vars.put(r, rId);
        vars.put(x, xId);
        vars.put(y, yId);
        vars.put(z, zId);

        GraqlInsert query = Graql.insert(var(r)
                        .isa("employment")
                        .rel("employee", var(x))
                        .rel("employee", var(y))
                        .rel("employee", var(z)),
                var(x).id(xId),
                var(y).id(yId),
                var(z).id(zId));

        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals(rId, insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenAttributeInserted_identifyAttributeWasInserted() {
        String xId = "Vx";
        String yId = "Vy";

        String cAttr = "c-name";

        Variable x = new Variable("x").asReturnedVar();
        Variable y = new Variable("y").asReturnedVar();

        HashMap<Variable, String> vars = new HashMap<>();
        vars.put(x, xId);
        vars.put(y, yId);

        GraqlInsert query = Graql.insert(var(x).isa("company").has("name", var(y)), var(x).id(xId), var(y).val(cAttr));

        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals(yId, insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenAttributeInsertedWithIdInClause_identifyAttributeWasInserted() {
        String xId = "Vx";
        String yId = "Vy";

        String cAttr = "c-name";

        Variable x = new Variable("x").asReturnedVar();
        Variable y = new Variable("y").asReturnedVar();

        HashMap<Variable, String> vars = new HashMap<>();
        vars.put(x, xId);
        vars.put(y, yId);

        GraqlInsert query = Graql.insert(var(x).isa("company").has("name", var(y)).id(xId), var(y).val(cAttr));

        ArrayList<ConceptMap> answerList = this.mockConceptMaps(vars);

        HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(query, answerList);

        assertEquals(1, insertedConcepts.size());
        assertEquals(yId, insertedConcepts.iterator().next().asThing().id().toString());
    }

    @Test
    public void whenInsertRelationship_identifyRolePlayers() {
        Variable xVar = new Variable("x").asReturnedVar();
        Variable yVar = new Variable("y").asReturnedVar();
        Statement x = var(xVar).id("V123");
        Statement y = var(yVar).id("V234");
        GraqlInsert insertQuery = Graql.match(x, y).insert(var("r").rel("friend", x).rel("friend", y).isa("friendship"));

        ConceptMap map = mock(ConceptMap.class);
        Concept xConcept = mock(Concept.class);
        when(xConcept.id()).thenReturn(ConceptId.of("V123"));
        Concept yConcept = mock(Concept.class);
        when(yConcept.id()).thenReturn(ConceptId.of("V234"));
        when(map.get(xVar)).thenReturn(xConcept);
        when(map.get(yVar)).thenReturn(yConcept);

        Map<String, List<Concept>> rolePlayers = InsertQueryAnalyser.getRolePlayersAndRoles(insertQuery, Arrays.asList(map));

        assertEquals(1, rolePlayers.size());
        assertEquals(2, rolePlayers.get("friend").size());
        assertTrue(rolePlayers.get("friend").contains(xConcept));
        assertTrue(rolePlayers.get("friend").contains(yConcept));
    }

    @Test
    public void whenInsertNonRelationship_returnEmptySet() {
        Variable x = new Variable("x").asReturnedVar();
        Variable y = new Variable("y").asReturnedVar();
        GraqlInsert insertQuery = Graql.insert(var(x).isa("company").has("name", var(y)).id("V123"), var(y).val("john"));

        ConceptMap map = mock(ConceptMap.class);
        Concept xConcept = mock(Concept.class);
        when(xConcept.id()).thenReturn(ConceptId.of("V123"));
        Concept yConcept = mock(Concept.class);
        when(yConcept.id()).thenReturn(ConceptId.of("V234"));
        when(map.get(x)).thenReturn(xConcept);
        when(map.get(y)).thenReturn(yConcept);

        Map<String, List<Concept>> rolePlayers = InsertQueryAnalyser.getRolePlayersAndRoles(insertQuery, Arrays.asList(map));
        assertEquals(0, rolePlayers.size());
    }

    @Test
    public void whenRelationshipInserted_relationshipLabelFound() {
        Statement x = var( new Variable("x").asReturnedVar()).id("V123");
        Statement y = var(new Variable("y").asReturnedVar()).id("V234");
        GraqlInsert insertQuery = Graql.match(x, y).insert(var("r").rel("friend", x).rel("friend", y).isa("friendship"));
        String relationshipLabel = InsertQueryAnalyser.getRelationshipTypeLabel(insertQuery);
        assertEquals("friendship", relationshipLabel);
    }

    @Test
    public void whenNoRelationshipInserted_nullReturned() {
        Statement x = var( new Variable("x").asReturnedVar());
        Statement y = var(new Variable("y").asReturnedVar());
        GraqlInsert insertQuery = Graql.insert(x.isa("company").has("name", y).id("V123"), y.val("john"));
        String relationshipLabel = InsertQueryAnalyser.getRelationshipTypeLabel(insertQuery);
        assertEquals(null, relationshipLabel);
    }

    @Test
    public void whenSameConceptPlaysTwoRoles_conceptRolePairReturnedTwice() {
        Variable xVar = new Variable("x").asReturnedVar();
        Statement x = var(xVar).id("V123");
        GraqlInsert insertQuery = Graql.match(x).insert(var("r").rel("friend", x).rel("friend", x).isa("friendship"));

        ConceptMap map = mock(ConceptMap.class);
        Concept xConcept = mock(Concept.class);
        when(xConcept.id()).thenReturn(ConceptId.of("V123"));
        when(map.get(xVar)).thenReturn(xConcept);

        Map<String, List<Concept>> mapping = InsertQueryAnalyser.getRolePlayersAndRoles(insertQuery, Arrays.asList(map));
        assertEquals(2, mapping.get("friend").size());
    }

}
