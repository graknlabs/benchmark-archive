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

package grakn.benchmark.runner.pick;

import grakn.core.client.Grakn;
import grakn.benchmark.runner.storage.ConceptTypeCountStore;

import java.util.Random;

import static grakn.core.graql.Graql.var;

/**
 *
 */
@Deprecated
public class IsaTypeConceptIdPicker extends ConceptIdPicker {

    private ConceptTypeCountStore conceptTypeCountStore;
    private String typeLabel;

    public IsaTypeConceptIdPicker(Random rand, ConceptTypeCountStore conceptTypeCountStore, String typeLabel) {
        super(rand, var("x").isa(typeLabel), var("x"));
        this.conceptTypeCountStore = conceptTypeCountStore;
        this.typeLabel = typeLabel;
    }

    @Override
    public Integer getConceptCount(Grakn.Transaction tx) {
        return conceptTypeCountStore.get(this.typeLabel);
    }
}