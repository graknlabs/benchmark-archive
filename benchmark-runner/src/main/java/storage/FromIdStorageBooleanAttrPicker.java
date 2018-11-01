package storage;

import ai.grakn.client.Grakn;

import java.util.Random;
import java.util.stream.Stream;

public class FromIdStorageBooleanAttrPicker extends FromIdStoragePicker<Boolean> {

    public FromIdStorageBooleanAttrPicker(Random rand, IdStoreInterface conceptStore, String typeLabel) {
        super(rand, conceptStore, typeLabel);
    }

    @Override
    public Stream<Boolean> getStream(Grakn.Transaction tx) {
        return null;
    }
}
