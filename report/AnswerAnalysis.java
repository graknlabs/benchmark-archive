package grakn.benchmark.report;

import grakn.core.concept.ConceptId;
import grakn.core.concept.answer.Answer;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.answer.ConceptSet;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;
import java.util.Set;

public class AnswerAnalysis {

    public static int insertedConcepts(GraqlInsert insertQuery, ConceptMap answer) {
        return answer.map().size();
    }

    public static int retrievedConcepts(GraqlGet getQuery, List<ConceptMap> answer) {
        return answer.stream()
                .map(conceptMap -> conceptMap.map().size())
                .reduce((a,b) -> a+b)
                .get();
    }

    public static int deletedConcepts(GraqlDelete deleteQuery, ConceptSet answer) {
        return answer.set().size();
    }

    public static int computedConcepts(GraqlCompute computeQuery, List<? extends Answer> answer) {
        // TODO
        return -1;
    }

    public static int roundTripsCompleted(GraqlInsert inserQuert, ConceptMap answer) {
        return 2;
    }

    public static int roundTripsCompleted(GraqlGet getQuery, List<ConceptMap> answer) {
        int baseRoundTrips = 2; // 1 - open query, 1 - iterator exhausted
        return baseRoundTrips + answer.size();
    }

    public static int roundTripsCompleted(GraqlDelete deleteQuery, ConceptSet answer) {
        return 2;
    }
}

