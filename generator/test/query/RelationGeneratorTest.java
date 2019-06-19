/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
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
import grakn.benchmark.generator.provider.key.CentralConceptKeyProvider;
import grakn.benchmark.generator.provider.key.ConceptKeyProvider;
import grakn.benchmark.generator.provider.key.CountingKeyProvider;
import grakn.benchmark.generator.strategy.RelationStrategy;
import grakn.benchmark.generator.strategy.RolePlayerTypeStrategy;
import graql.lang.query.GraqlInsert;
import org.junit.Test;

import java.util.ArrayList;
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

        List<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new ArrayList<>();
        RolePlayerTypeStrategy rolePlayerTypeStrategy = mock(RolePlayerTypeStrategy.class);
        rolePlayerTypeStrategies.add(rolePlayerTypeStrategy);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));
        when(strategy.getConceptKeyProvider()).thenReturn(new CountingKeyProvider(0));

        List<Long> conceptKeyList = Arrays.asList(1L);
        CentralConceptKeyProvider centralConceptKeyProvider = mock(CentralConceptKeyProvider.class); //(new FixedConstant(3), conceptIdList.iterator());
        when(rolePlayerTypeStrategy.getConceptKeyProvider()).thenReturn(centralConceptKeyProvider);
        when(centralConceptKeyProvider.hasNext()).thenReturn(true);
        when(centralConceptKeyProvider.next()).thenReturn(conceptKeyList.get(0));

        RelationGenerator relationshipQueryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = relationshipQueryGenerator.generate();

        verify(centralConceptKeyProvider, times(1)).resetUniqueness();
    }

    @Test
    public void whenUsingMultipleRolesWithPdf2_allRolePlayersFilledTwice() {

        RelationStrategy strategy = mock(RelationStrategy.class);

        List<Long> ownerRolePlayerKeys = Arrays.asList(1L, 2L);
        ConceptKeyProvider ownerKeyProvider = mock(ConceptKeyProvider.class);
        when(ownerKeyProvider.next()).thenReturn(ownerRolePlayerKeys.get(0)).thenReturn(ownerRolePlayerKeys.get(1));
        when(ownerKeyProvider.hasNext()).thenReturn(true);
        when(ownerKeyProvider.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("owner", new FixedConstant(1), ownerKeyProvider);

        List<Long> propertyRolePlayerKeys = Arrays.asList(3L, 4L);
        ConceptKeyProvider propertyKeyProvider = mock(ConceptKeyProvider.class);
        when(propertyKeyProvider.next()).thenReturn(propertyRolePlayerKeys.get(0)).thenReturn(propertyRolePlayerKeys.get(1));
        when(propertyKeyProvider.hasNext()).thenReturn(true);
        when(propertyKeyProvider.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("property", new FixedConstant(1), propertyKeyProvider);

        List<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new ArrayList<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("ownership");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));
        when(strategy.getConceptKeyProvider()).thenReturn(new CountingKeyProvider(0));


        RelationGenerator queryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = queryGenerator.generate();

        assertTrue(queries.hasNext());
        GraqlInsert firstInsert = queries.next();
        String queryString = firstInsert.toString();
        assertTrue(queryString.contains("owner: ") && queryString.contains("unique-key 1"));
        assertTrue(queryString.contains("property: ") && queryString.contains("unique-key 3"));

        assertTrue(queries.hasNext());
        GraqlInsert secondInsert = queries.next();
        queryString = secondInsert.toString();
        assertTrue(queryString.contains("owner: ") && queryString.contains("unique-key 2"));
        assertTrue(queryString.contains("property: ") && queryString.contains("unique-key 4"));

        assertFalse(queries.hasNext());
    }

    @Test
    public void whenRepeatedRole_roleIsRepeatedInQuery() {
        RelationStrategy strategy = mock(RelationStrategy.class);

        List<Long> friendRolePlayerKeys1 = Arrays.asList(1L, 2L);
        ConceptKeyProvider ownerKeyProvider = mock(ConceptKeyProvider.class);
        when(ownerKeyProvider.next()).thenReturn(friendRolePlayerKeys1.get(0)).thenReturn(friendRolePlayerKeys1.get(1));
        when(ownerKeyProvider.hasNext()).thenReturn(true);
        when(ownerKeyProvider.hasNextN(2)).thenReturn(true).thenReturn(false);
        // 2 FRIEND role players contributed here
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("friend", new FixedConstant(2), ownerKeyProvider);

        List<Long> friendRolePlayerKeys2 = Arrays.asList(3L);
        ConceptKeyProvider propertyKeyProvider = mock(ConceptKeyProvider.class);
        when(propertyKeyProvider.next()).thenReturn(friendRolePlayerKeys2.get(0));
        when(propertyKeyProvider.hasNext()).thenReturn(true);
        when(propertyKeyProvider.hasNextN(1)).thenReturn(true).thenReturn(false);
        // 1 FRIEND role player contributed here
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("friend", new FixedConstant(1), propertyKeyProvider);

        List<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new ArrayList<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(1));
        when(strategy.getConceptKeyProvider()).thenReturn(new CountingKeyProvider(0));

        RelationGenerator queryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = queryGenerator.generate();

        assertTrue(queries.hasNext());
        GraqlInsert firstInsert = queries.next();
        String queryString = firstInsert.toString();
        assertTrue(queryString.contains("unique-key 1") && queryString.contains("unique-key 2") && queryString.contains("unique-key 3"));
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
        List<Long> friendRolePlayerKeys1 = Arrays.asList(1L, 2L);
        ConceptKeyProvider ownerKeyProvider = mock(ConceptKeyProvider.class);
        when(ownerKeyProvider.next()).thenReturn(friendRolePlayerKeys1.get(0)).thenReturn(friendRolePlayerKeys1.get(1));
        when(ownerKeyProvider.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(ownerKeyProvider.hasNextN(2)).thenReturn(true).thenReturn(false);
        // 2 FRIEND role players contributed here
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("friend", new FixedConstant(2), ownerKeyProvider);

        List<Long> friendRolePlayerKeys2 = Arrays.asList(3L, 4L);
        ConceptKeyProvider propertyKeyProvider = mock(ConceptKeyProvider.class);
        when(propertyKeyProvider.next()).thenReturn(friendRolePlayerKeys2.get(0)).thenReturn(friendRolePlayerKeys2.get(1));
        when(propertyKeyProvider.hasNext()).thenReturn(true);
        when(propertyKeyProvider.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        // 1 FRIEND role player contributed here
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("friend", new FixedConstant(1), propertyKeyProvider);

        List<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new ArrayList<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        // target: generate two relationships
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));
        when(strategy.getConceptKeyProvider()).thenReturn(new CountingKeyProvider(0));

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
        List<Long> friendRolePlayerKeys1 = Arrays.asList(1L, 2L, 5L);
        ConceptKeyProvider friendKeyProvider = mock(ConceptKeyProvider.class);
        when(friendKeyProvider.next()).thenReturn(friendRolePlayerKeys1.get(0)).thenReturn(friendRolePlayerKeys1.get(1)).thenReturn(friendRolePlayerKeys1.get(2));
        when(friendKeyProvider.hasNextN(2)).thenReturn(true).thenReturn(false);
        when(friendKeyProvider.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        // 2 FRIEND role players contributed here
        RolePlayerTypeStrategy rolePlayer1 = new RolePlayerTypeStrategy("friend", new FixedConstant(2), friendKeyProvider);

        List<Long> friendRolePlayerKeys2 = Arrays.asList(3L, 4L);
        ConceptKeyProvider friendKeyProvider2 = mock(ConceptKeyProvider.class);
        when(friendKeyProvider2.next()).thenReturn(friendRolePlayerKeys2.get(0)).thenReturn(friendRolePlayerKeys2.get(1));
        when(friendKeyProvider2.hasNextN(1)).thenReturn(true).thenReturn(true).thenReturn(false);
        when(friendKeyProvider2.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        // 1 FRIEND role player contributed here
        RolePlayerTypeStrategy rolePlayer2 = new RolePlayerTypeStrategy("friend", new FixedConstant(1), friendKeyProvider2);

        List<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new ArrayList<>();
        rolePlayerTypeStrategies.add(rolePlayer1);
        rolePlayerTypeStrategies.add(rolePlayer2);

        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
        when(strategy.getTypeLabel()).thenReturn("friendship");
        // target: generate two relationships
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(2));
        when(strategy.getConceptKeyProvider()).thenReturn(new CountingKeyProvider(0));

        RelationGenerator queryGenerator = new RelationGenerator(strategy);
        Iterator<GraqlInsert> queries = queryGenerator.generate();

        assertTrue(queries.hasNext());
        GraqlInsert firstInsert = queries.next();
        assertFalse(queries.hasNext());
    }
}
