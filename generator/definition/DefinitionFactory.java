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

package grakn.benchmark.generator.definition;

import grakn.benchmark.generator.storage.ConceptStorage;

import java.util.Random;

public class DefinitionFactory {

    public static DataGeneratorDefinition getDefinition(String name, Random random, ConceptStorage storage) {
        switch (name) {
            case "social_network":
                return new SocialNetworkDefinition(random, storage);
            case "road_network":
                return new RoadNetworkDefinition(random, storage);
            case "biochemical_network":
                return new BiochemicalNetworkDefinition(random, storage);
            case "financial":
                return new FinancialTransactionsDefinition(random, storage);
            case "generic_uniform_network":
                return new GenericUniformNetworkDefinition(random, storage);
            default:
                throw new RuntimeException("Unknown specific schema generation strategy name: " + name);
        }
    }
}

