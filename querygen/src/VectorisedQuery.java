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

import grakn.benchmark.querygen.subsampling.Vectorisable;
import graql.lang.query.GraqlGet;

import java.util.Arrays;
import java.util.List;

/**
 * A GraqlGet query with the measures about the query, computed via
 */
public class VectorisedQuery implements Vectorisable {

    GraqlGet graqlQuery;

    private double numVariables;
    private double meanRolesPerRelation;
    private double meanUniqueRolesPerRelation;
    private double meanAttributesOwnedPerThing;
    private double ambiguity;
    private double specificity;
    private double meanEdgesPerVariable;
    private double comparisonsPerAttribute;


    public VectorisedQuery(GraqlGet graqlQuery, Vectoriser queryVectoriser) {
        this.graqlQuery = graqlQuery;

        numVariables = queryVectoriser.numVariables();
        meanRolesPerRelation = queryVectoriser.meanRolesPerRelation();
        meanUniqueRolesPerRelation = queryVectoriser.meanUniqueRolesPerRelation();
        meanAttributesOwnedPerThing = queryVectoriser.meanAttributesOwnedPerThing();
        ambiguity = queryVectoriser.ambiguity();
        specificity = queryVectoriser.specificity();
        meanEdgesPerVariable = queryVectoriser.meanEdgesPerVariable();
        comparisonsPerAttribute = queryVectoriser.comparisonsPerAttribute();
    }

    @Override
    public List<Double> asVector() {
        return Arrays.asList(
                numVariables,
                meanRolesPerRelation,
                meanUniqueRolesPerRelation,
                meanAttributesOwnedPerThing,
                ambiguity,
                specificity,
                meanEdgesPerVariable,
                comparisonsPerAttribute
        );
    }

}
