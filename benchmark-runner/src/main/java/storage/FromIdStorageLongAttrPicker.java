package storage;

import ai.grakn.client.Grakn;

import java.util.Random;
import java.util.stream.Stream;

public class FromIdStorageLongAttrPicker extends FromIdStoragePicker<Long> {

    public FromIdStorageLongAttrPicker(Random rand, IdStoreInterface conceptStore, String typeLabel) {
        super(rand, conceptStore, typeLabel);
    }

    @Override
    public Stream<Long> getStream(Grakn.Transaction tx) {
        return null;
    }
}
