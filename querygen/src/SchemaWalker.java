/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import grakn.core.concept.type.Type;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class SchemaWalker {

    /**
     * Implementation may vary - either a true random walk implemented using sub!, or a random picking of the subtype
     *
     * @param rootType starting type
     * @return some type that is a subtype of rootType
     */
    static Type walkSubs(Type rootType, Random random) {
        List<Type> subs = rootType.subs()
                .sorted(Comparator.comparing(type -> type.label().toString()))
                .collect(Collectors.toList());
        int index = random.nextInt(subs.size());
        return subs.get(index);
    }

    static Type walkSupsNoMeta(GraknClient.Transaction tx, Type ownableAttribute, Random random) {
        Type metaConcept = tx.getMetaConcept();
        List<? extends Type> nonMetaSups = ownableAttribute.sups()
                .filter(type -> !type.equals(metaConcept))
                .sorted(Comparator.comparing(type -> type.label().toString()))
                .collect(Collectors.toList());


        int index = random.nextInt(nonMetaSups.size());
        return nonMetaSups.get(index);
    }
}
