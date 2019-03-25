/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 GraknClient Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.generator.query;

import grakn.benchmark.generator.probdensity.FixedConstant;
import grakn.benchmark.generator.provider.concept.CentralConceptProvider;
import grakn.benchmark.generator.provider.concept.ConceptIdProvider;
import grakn.benchmark.generator.strategy.RelationStrategy;
import grakn.benchmark.generator.strategy.RolePlayerTypeStrategy;
import grakn.core.concept.ConceptId;
import graql.lang.query.GraqlInsert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RelationGeneratorTest {

    @Test
    public void whenUsingCentralRolePlayerProvider_resetIsCalled() {

        RelationStrategy strategy = mock(RelationStrategy.class);

        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new HashSet<>();
        RolePlayerTypeStrategy rolePlayerTypeStrategy = mock(RolePlayerTypeStrategy.class);
        rolePlayerTypeStrategies.add(rolePlayerTypeStrategy);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));

        List<ConceptId> conceptIdList = Arrays.asList(ConceptId.of("a"));
        CentralConceptProvider centralConceptProvider = mock(CentralConceptProvider.class); //(new FixedConstant(3), conceptIdList.iterator());
        when(rolePlayerTypeStrategy.getConceptProvider()).thenReturn(centralConceptProvider);
        when(centralConceptProvider.hasNext()).thenReturn(true);
        when(centralConceptProvider.next()).thenReturn(conceptIdList.get(0));


        RelationGenerator relationshipQueryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = relationshipQueryGenerator.generate();

        verify(centralConceptProvider, times(1)).resetUniqueness();
    }

    @Test
    public void whenUsingMultipleRolesWithPdf2_allRolePlayersFilledTwice() {

        RelationStrategy strategy = mock(RelationStrategy.class);

        List<ConceptId> ownerRolePlayers = Arrays.asList(ConceptId.of("a"), ConceptId.of("b"));
        ConceptIdProvider ownerIdProvider = mock(ConceptIdProvider.class);
        when(ownerIdProvider.next()).thenReturn(ownerRolePlayers.get(0)).thenReturn(ownerRolePlayers.get(1));
        when(ownerIdProvider.hasNext()).thenReturn(true);
        when(ownerIdProvider.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("owner", new FixedConstant(1), ownerIdProvider);

        List<ConceptId> propertyRolePlayers = Arrays.asList(ConceptId.of("c"), ConceptId.of("d"));
        ConceptIdProvider propertyIdProvider = mock(ConceptIdProvider.class);
        when(propertyIdProvider.next()).thenReturn(propertyRolePlayers.get(0)).thenReturn(propertyRolePlayers.get(1));
        when(propertyIdProvider.hasNext()).thenReturn(true);
        when(propertyIdProvider.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("property", new FixedConstant(1), propertyIdProvider);

        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new HashSet<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("ownership");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));


        RelationGenerator queryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = queryGenerator.generate();

        assertTrue(queries.hasNext());
        GraqlInsert firstInsert = queries.next();
        String queryString = firstInsert.toString();
        assertTrue(queryString.contains("owner: ") && queryString.contains("id a"));
        assertTrue(queryString.contains("property: ") && queryString.contains("id c"));

        assertTrue(queries.hasNext());
        GraqlInsert secondInsert = queries.next();
        queryString = secondInsert.toString();
        assertTrue(queryString.contains("owner: ") && queryString.contains("id b"));
        assertTrue(queryString.contains("property: ") && queryString.contains("id d"));

        assertFalse(queries.hasNext());
    }

    @Test
    public void whenRepeatedRole_roleIsRepeatedInQuery() {
        RelationStrategy strategy = mock(RelationStrategy.class);

        List<ConceptId> friendRolePlayers1 = Arrays.asList(ConceptId.of("a"), ConceptId.of("b"));
        ConceptIdProvider ownerIdProvider = mock(ConceptIdProvider.class);
        when(ownerIdProvider.next()).thenReturn(friendRolePlayers1.get(0)).thenReturn(friendRolePlayers1.get(1));
        when(ownerIdProvider.hasNext()).thenReturn(true);
        when(ownerIdProvider.hasNextN(2)).thenReturn(true).thenReturn(false);
        // 2 FRIEND role players contributed here
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("friend", new FixedConstant(2), ownerIdProvider);

        List<ConceptId> friendRolePlayers2 = Arrays.asList(ConceptId.of("c"));
        ConceptIdProvider propertyIdProvider = mock(ConceptIdProvider.class);
        when(propertyIdProvider.next()).thenReturn(friendRolePlayers2.get(0));
        when(propertyIdProvider.hasNext()).thenReturn(true);
        when(propertyIdProvider.hasNextN(1)).thenReturn(true).thenReturn(false);
        // 1 FRIEND role player contributed here
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("friend", new FixedConstant(1), propertyIdProvider);

        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new HashSet<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(1));

        RelationGenerator queryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = queryGenerator.generate();

        assertTrue(queries.hasNext());
        GraqlInsert firstInsert = queries.next();
        String queryString = firstInsert.toString();
        assertTrue(queryString.contains("id a") && queryString.contains("id b") && queryString.contains("id c"));
        // want to check that "friend" occurs three times in the string
        int firstIndex = queryString.indexOf("friend:", 0);
        int secondIndex = queryString.indexOf("friend:", firstIndex + 7);
        int thirdIndex = queryString.indexOf("friend:", secondIndex + 7);
        int nonIndex = queryString.indexOf("friend:", thirdIndex + 7);

        assertTrue(firstIndex > 0);
        assertTrue(secondIndex > 0);
        assertTrue(thirdIndex > 0);
        assertTrue(nonIndex == -1);
        assertFalse(queries.hasNext());
    }

    @Test
    public void whenRoleProvidersHaveDifferentAvailability_generateFewerInsertQueries() {
        RelationStrategy strategy = mock(RelationStrategy.class);

        // this RolePlayer filler will only have enough for ONE relationship with two role players
        List<ConceptId> friendRolePlayers1 = Arrays.asList(ConceptId.of("a"), ConceptId.of("b"));
        ConceptIdProvider ownerIdProvider = mock(ConceptIdProvider.class);
        when(ownerIdProvider.next()).thenReturn(friendRolePlayers1.get(0)).thenReturn(friendRolePlayers1.get(1));
        when(ownerIdProvider.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(ownerIdProvider.hasNextN(2)).thenReturn(true).thenReturn(false);
        // 2 FRIEND role players contributed here
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("friend", new FixedConstant(2), ownerIdProvider);

        List<ConceptId> friendRolePlayers2 = Arrays.asList(ConceptId.of("c"), ConceptId.of("d"));
        ConceptIdProvider propertyIdProvider = mock(ConceptIdProvider.class);
        when(propertyIdProvider.next()).thenReturn(friendRolePlayers2.get(0)).thenReturn(friendRolePlayers2.get(1));
        when(propertyIdProvider.hasNext()).thenReturn(true);
        when(propertyIdProvider.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        // 1 FRIEND role player contributed here
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("friend", new FixedConstant(1), propertyIdProvider);

        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new HashSet<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        // target: generate two relationships
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));

        RelationGenerator queryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = queryGenerator.generate();

        assertTrue(queries.hasNext());
        GraqlInsert firstInsert = queries.next();
        assertFalse(queries.hasNext());
    }

    @Test
    public void whenARoleProviderHasTooFewPlayers_generateFewerQueries() {
        RelationStrategy strategy = mock(RelationStrategy.class);

        // this RolePlayer filler will only have enough for 1.5 relationship with two role players
        List<ConceptId> friendRolePlayers1 = Arrays.asList(ConceptId.of("a"), ConceptId.of("b"), ConceptId.of("e"));
        ConceptIdProvider friendIdProvider = mock(ConceptIdProvider.class);
        when(friendIdProvider.next()).thenReturn(friendRolePlayers1.get(0)).thenReturn(friendRolePlayers1.get(1)).thenReturn(friendRolePlayers1.get(2));
        when(friendIdProvider.hasNextN(2)).thenReturn(true).thenReturn(false);
        when(friendIdProvider.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        // 2 FRIEND role players contributed here
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("friend", new FixedConstant(2), friendIdProvider);

        List<ConceptId> friendRolePlayers2 = Arrays.asList(ConceptId.of("c"), ConceptId.of("d"));
        ConceptIdProvider friendIdProvider2 = mock(ConceptIdProvider.class);
        when(friendIdProvider2.next()).thenReturn(friendRolePlayers2.get(0)).thenReturn(friendRolePlayers2.get(1));
        when(friendIdProvider2.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        when(friendIdProvider2.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        // 1 FRIEND role player contributed here
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("friend", new FixedConstant(1), friendIdProvider2);

        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new HashSet<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        // target: generate two relationships
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));

        RelationGenerator queryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = queryGenerator.generate();

        assertTrue(queries.hasNext());
        GraqlInsert firstInsert = queries.next();
        assertFalse(queries.hasNext());
    }
}
