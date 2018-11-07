package ai.grakn.benchmark.runner.storage;

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
        Stream<Integer> randomUniqueOffsetStream = this.getStreamOfRandomOffsets(tx);
        return randomUniqueOffsetStream.map(randomOffset -> this.conceptStore.getDate(this.typeLabel, randomOffset));
    }

}
