package grakn.benchmark.runner.storage;

import grakn.core.client.Grakn;

import java.util.Random;
import java.util.stream.Stream;

public class FromIdStorageStringAttrPicker extends FromIdStoragePicker<String> {

    public FromIdStorageStringAttrPicker(Random rand, IdStoreInterface conceptStore, String typeLabel) {
        super(rand, conceptStore, typeLabel);
    }

    @Override
    public Stream<String> getStream(Grakn.Transaction tx) {
        Stream<Integer> randomUniqueOffsetStream = this.getStreamOfRandomOffsets(tx);
        return randomUniqueOffsetStream.map(randomOffset -> this.conceptStore.getString(this.typeLabel, randomOffset));
    }
}
