package storage;

import ai.grakn.client.Grakn;
import ai.grakn.concept.ConceptId;

import java.util.Random;
import java.util.stream.Stream;

public class FromIdStorageConceptIdPicker extends FromIdStoragePicker<ConceptId> {

    public FromIdStorageConceptIdPicker(Random rand, IdStoreInterface conceptStore, String typeLabel) {
        super(rand, conceptStore, typeLabel);
    }

    @Override
    public Stream<ConceptId> getStream(Grakn.Transaction tx) {
        return null;
    }
}
