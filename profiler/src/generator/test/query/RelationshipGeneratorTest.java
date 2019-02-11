package grakn.benchmark.profiler.generator.query;

import grakn.benchmark.profiler.generator.strategy.RelationshipStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RelationshipGeneratorTest {


    @Test
    public void whenUsingCentralRolePlayerProvider_resetIsCalled() {

//        RelationshipStrategy strategy = mock(RelationshipStrategy.class);
//
//        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = new HashSet<>();
//        RolePlayerTypeStrategy rolePlayerTypeStrategy = mock(RolePlayerTypeStrategy.class);
//        rolePlayerTypeStrategies.add(rolePlayerTypeStrategy);
//
//        when(strategy.getRolePlayerTypeStrategies()).thenReturn(rolePlayerTypeStrategies);
//        when(strategy.getTypeLabel()).



    }

    @Test
    public void whenUsingMultipleRoles_allRolePlayersFilled() {

    }

    @Test
    public void whenRepeatedRole_roleIsRepeatedInQuery() {

    }

    @Test
    public void whenPdfConstantTwo_generateTwoInsertQueries() {

    }

    @Test
    public void whenRoleProviderHasFewerPlayers_generateFewerInsertQueries() {

    }
}
