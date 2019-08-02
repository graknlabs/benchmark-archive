package grakn.benchmark.querygen;

import grakn.core.concept.type.Type;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class SchemaWalker {

    /**
     * Implementation may vary - either a true random walk implemented using sub!, or a random picking of the subtype
     * @param rootType starting type
     * @return some type that is a subtype of rootType
     */
    public static Type walkSub(Type rootType, Random random) {
        List<Type> subs = rootType.subs().collect(Collectors.toList());
        int index = random.nextInt(subs.size());
        return subs.get(index);
    }

}
