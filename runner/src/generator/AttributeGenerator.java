/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
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

package grakn.benchmark.runner.generator;

import grakn.core.client.Grakn;
import grakn.core.concept.ConceptId;
import grakn.core.graql.Graql;
import grakn.core.graql.Query;
import grakn.core.graql.QueryBuilder;
import grakn.core.graql.Var;
import grakn.benchmark.runner.probdensity.FixedConstant;
import grakn.benchmark.runner.pick.StreamProviderInterface;
import grakn.benchmark.runner.strategy.AttributeOwnerTypeStrategy;
import grakn.benchmark.runner.strategy.AttributeStrategy;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * @param <OwnerDatatype>
 * @param <ValueDatatype>
 */
public class AttributeGenerator<OwnerDatatype, ValueDatatype> extends Generator<AttributeStrategy<OwnerDatatype, ValueDatatype>> {

    /**
     * @param strategy
     * @param tx
     */
    public AttributeGenerator(AttributeStrategy<OwnerDatatype, ValueDatatype> strategy, Grakn.Transaction tx) {
        super(strategy, tx);
    }

    /**
     * @return
     */
    @Override
    public Stream<Query> generate() {
        QueryBuilder qb = this.tx.graql();
        int numInstances = this.strategy.getNumInstancesPDF().sample();

        StreamProviderInterface<ValueDatatype> valuePicker = this.strategy.getPicker();
        valuePicker.reset();
        FixedConstant unityPDF = new FixedConstant(1);

        String attributeTypeLabel = this.strategy.getTypeLabel();

        return Stream.generate(() -> {
            Var attr = Graql.var().asUserDefined();
            Stream<ValueDatatype> valueStream = valuePicker.getStream(unityPDF, tx);
            ValueDatatype value = valueStream.findFirst().get();
            return (Query) qb.insert(attr.isa(attributeTypeLabel), attr.val(value));
        }).limit(numInstances).filter(Objects::nonNull);
    }
}
