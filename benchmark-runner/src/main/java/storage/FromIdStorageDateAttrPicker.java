package storage;

import ai.grakn.client.Grakn;

import java.util.Date;
import java.util.Random;
import java.util.stream.Stream;

public class FromIdStorageDateAttrPicker extends FromIdStoragePicker<Date> {

    public FromIdStorageDateAttrPicker(Random rand, IdStoreInterface conceptStore, String typeLabel) {
        super(rand, conceptStore, typeLabel);
    }

    @Override
    public Stream<Date> getStream(Grakn.Transaction tx) {
        return null;
    }
}
