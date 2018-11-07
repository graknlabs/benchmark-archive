package ai.grakn.benchmark.runner.storage;

import ai.grakn.client.Grakn;

import java.util.Random;
import java.util.stream.Stream;

public class FromIdStorageLongAttrPicker extends FromIdStoragePicker<Long> {

    public FromIdStorageLongAttrPicker(Random rand, IdStoreInterface conceptStore, String typeLabel) {
        super(rand, conceptStore, typeLabel);
    }

    @Override
    public Stream<Long> getStream(Grakn.Transaction tx) {
        Stream<Integer> randomUniqueOffsetStream = this.getStreamOfRandomOffsets(tx);
        return randomUniqueOffsetStream.map(randomOffset -> this.conceptStore.getLong(this.typeLabel, randomOffset));
    }

}
