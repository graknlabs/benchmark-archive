package grakn.benchmark.profiler.generator.provider;

import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.core.concept.ConceptId;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class NotInRelationshipConceptIdProvider implements Iterator<ConceptId> {

    private String relationshipLabel;
    private String roleLabel;
    private final Random rand;
    private String typeLabel;
    ConceptStorage conceptStorage;

    public NotInRelationshipConceptIdProvider(Random rand,
                                              ConceptStorage conceptStorage,
                                              String rolePlayerTypeLabel,
                                              String relationshipLabel,
                                              String roleLabel
    ) {
        this.rand = rand;
        this.typeLabel = rolePlayerTypeLabel;
        this.relationshipLabel = relationshipLabel;
        this.roleLabel = roleLabel;
        this.conceptStorage = conceptStorage;
    }


    @Override
    public boolean hasNext() {
        return !conceptStorage.getIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel).isEmpty();
    }

    @Override
    public ConceptId next() {
        List<String> notInRelationshipConceptIds = conceptStorage.getIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel);
        int randomOffset = rand.nextInt(notInRelationshipConceptIds.size());
        return ConceptId.of(notInRelationshipConceptIds.get(randomOffset));
    }

}
