package grakn.benchmark.runner.storage;

import grakn.core.client.Grakn;
import grakn.core.concept.ConceptId;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class NotInRelationshipConceptIdPicker extends FromIdStoragePicker<ConceptId> {

    private String relationshipLabel;
    private String roleLabel;

    public NotInRelationshipConceptIdPicker(Random rand,
                                            IdStoreInterface conceptStore,
                                            String rolePlayerTypeLabel,
                                            String relationshipLabel,
                                            String roleLabel
                                            ) {
        super(rand, conceptStore, rolePlayerTypeLabel);
        this.typeLabel = rolePlayerTypeLabel;
        this.relationshipLabel = relationshipLabel;
        this.roleLabel = roleLabel;

    }

    @Override
    public Stream<ConceptId> getStream(Grakn.Transaction tx) {
        Stream<Integer> randomUniqueOffsetStream = this.getStreamOfRandomOffsets(tx);
        List<String> notInRelationshipConceptIds = conceptStore.getIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel);
        return randomUniqueOffsetStream.map(randomOffset -> ConceptId.of(notInRelationshipConceptIds.get(randomOffset)));
    }

    @Override
    public Integer getConceptCount(Grakn.Transaction tx) {
        return conceptStore.numIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel);
    }

}
