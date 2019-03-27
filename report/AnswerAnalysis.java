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

package grakn.benchmark.report;

import grakn.core.concept.answer.Answer;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.answer.ConceptSet;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;

public class AnswerAnalysis {

    public static int insertedConcepts(GraqlInsert insertQuery, ConceptMap answer) {
        return answer.map().size();
    }

    public static int retrievedConcepts(GraqlGet getQuery, List<ConceptMap> answer) {
        return answer.stream()
                .map(conceptMap -> conceptMap.map().size())
                .reduce((a,b) -> a+b)
                .orElse(0);
    }

    public static int deletedConcepts(GraqlDelete deleteQuery, ConceptSet answer) {
        return answer.set().size();
    }

    public static int computedConcepts(GraqlCompute computeQuery, List<? extends Answer> answer) {
        // TODO
        return -1;
    }

    public static int roundTripsCompleted(GraqlInsert inserQuert, ConceptMap answer) {
        return 3;
    }

    public static int roundTripsCompleted(GraqlGet getQuery, List<ConceptMap> answer) {
        int baseRoundTrips = 2; // 1 - open query, 1 - iterator exhausted
        return baseRoundTrips + answer.size();
    }

    public static int roundTripsCompleted(GraqlDelete deleteQuery, ConceptSet answer) {
        return 2;
    }
}

