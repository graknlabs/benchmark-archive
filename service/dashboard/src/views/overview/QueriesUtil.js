/* eslint-disable no-plusplus */
function getLegenedsData(queries) {
  let matchQuery = 0;
  let matchInsertQuery = 0;
  let insertQuery = 0;
  let computeQuery = 0;
  let otherQuery = 0;
  const queriesMap = {};
  queries.forEach((query) => {
    let value;
    if (query.includes('compute')) {
      value = `computeQuery${++computeQuery}`;
    } else if (query.includes('insert') && query.includes('match')) {
      value = `matchInsertQuery${++matchInsertQuery}`;
    } else if (query.includes('insert')) {
      value = `insertQuery${++insertQuery}`;
    } else if (query.includes('match')) {
      value = `matchQuery${++matchQuery}`;
    } else {
      value = `query${++otherQuery}`;
    }
    queriesMap[query] = value;
  });
  return queriesMap;
}

function computeAvgTime(spans) {
  return spans.reduce((a, b) => a + b.duration, 0) / spans.length;
}

function computeStdDeviation(spans, avgTime) {
  const sum = spans
    .map(span => (span.duration - avgTime) ** 2)
    .reduce((a, b) => a + b, 0);
  return Math.sqrt(sum / spans.length);
}

/**
 * Produces the data required for populating commit charts.
 *
 * Sample output is as follows:
 *  [
 *    {
 *      query: "...",
 *      times: [
 *        {
 *          commit: "...",
 *          executionId: "...",
 *          avgTime: 00.00,
 *          stdDeviation: 00.00,
 *          repetitions: 0,
 *        }
 *      ]
 *    }
 *  ]
 *
 * @param {String[]} queries unique list of Graql query values.
 * @param {Object[]} querySpans query spans containing executionId and scale, as well as the typical query span properties.
 * @param {Object[]} executions execution objects with the commit property.
 * @param {Number} selectedScale the scale currently selected by the user on the chart.
 *
 * @return {Object[]} the data required to populate the commit charts.
 */
function getChartData(queries, querySpans, executions, selectedScale) {
  return queries.map((query) => {
    const targetQuerySpans = querySpans.filter(querySpan => querySpan.value === query);
    const times = executions.map((execution) => {
      const executionQuerySpans = targetQuerySpans.filter(
        targetQuerySpan => targetQuerySpan.executionId === execution.id && targetQuerySpan.scale === selectedScale,
      );
      const avgTime = computeAvgTime(executionQuerySpans);
      const stdDeviation = computeStdDeviation(executionQuerySpans, avgTime);

      return {
        commit: execution.commit,
        executionId: execution.id,
        avgTime: avgTime / 1000,
        stdDeviation: stdDeviation / 1000,
        repetitions: executionQuerySpans.length,
      };
    });

    return { query, times };
  });
}

export default {
  getLegenedsData,
  getChartData,
};
