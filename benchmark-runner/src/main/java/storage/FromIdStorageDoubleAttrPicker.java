package storage;

import ai.grakn.client.Grakn;

import java.util.Random;
import java.util.stream.Stream;

public class FromIdStorageDoubleAttrPicker extends FromIdStoragePicker<Double> {

    public FromIdStorageDoubleAttrPicker(Random rand, IdStoreInterface conceptStore, String typeLabel) {
        super(rand, conceptStore, typeLabel);
    }

    @Override
    public Stream<Double> getStream(Grakn.Transaction tx) {
        return null;
    }
}
