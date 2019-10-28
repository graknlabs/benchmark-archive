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

package grakn.benchmark.report.producer;

import grakn.client.answer.Answer;
import grakn.client.answer.AnswerGroup;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.ConceptSet;
import grakn.client.answer.Void;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;

public class AnswerAnalysis {

    static int countInsertedConcepts(GraqlInsert insertQuery, ConceptMap answer) {
        return answer.map().size();
    }

    static int countRetrievedConcepts(GraqlGet getQuery, List<ConceptMap> answer) {
        return answer.stream()
                .map(conceptMap -> conceptMap.map().size())
                .reduce((a,b) -> a+b)
                .orElse(0);
    }

    public static int computedConcepts(GraqlCompute computeQuery, List<? extends Answer> answer) {
        // TODO
        return -1;
    }

    static int countRoundTripsCompleted(GraqlInsert inserQuert, ConceptMap answer) {
        return 3;
    }

    static int countRoundTripsCompleted(GraqlGet getQuery, List<ConceptMap> answer) {
        int baseRoundTrips = 2; // 1 - open query, 1 - iterator exhausted
        return baseRoundTrips + answer.size();
    }

    static int countRoundTripsCompleted(List<AnswerGroup<ConceptMap>> answer) {
        int baseRoundTrips = 2;
        return baseRoundTrips + answer.size();
    }

    static int countRoundTripsCompleted(GraqlDelete deleteQuery, Void answer) {
        return 2;
    }


    public static int countGroupedConcepts(List<AnswerGroup<ConceptMap>> answer) {
        int count = 0;
        for (AnswerGroup<ConceptMap> answerGroup : answer) {
            count += answerGroup.answers().size();
        }
        return count;
    }
}

