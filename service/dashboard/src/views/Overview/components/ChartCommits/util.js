import EDF from '@/util/ExecutionDataFormatters';

const { addExecutionIdAndScaleToQuerySpan } = EDF;

export const getCommitsChartOptions = async (graphs, querySpans, queries, executions, selectedScale) => {
  const querySpansWithExecutionAndScale = addExecutionIdAndScaleToQuerySpan(
    graphs,
    querySpans,
  );

  const chartData = getChartData(
    queries,
    querySpansWithExecutionAndScale,
    executions,
    selectedScale,
  );

  const legendsData = getLegendsData(queries);

  const series = chartData.map((data) => {
    const maxStdDeviation = 45;

    return {
      name: legendsData[data.query],
      type: 'line',
      data: data.times.map(dataItem => ({
        value: Number(dataItem.avgTime).toFixed(3),
        symbolSize:
            Math.min(dataItem.stdDeviation / 10, maxStdDeviation) + 5,
        symbol: 'circle',
        stdDeviation: dataItem.stdDeviation,
        repetitions: dataItem.repetitions,
        executionId: dataItem.executionId,
        itemStyle: {
          // draw the chart node with a different color when its standard deviation exceeds the given maximum amount
          color:
              dataItem.stdDeviation / 10 > maxStdDeviation ? '#666' : null,
        },
      })),
      smooth: true,
      emphasis: { label: { show: false }, itemStyle: { color: 'yellow' } },
      showAllSymbol: true,
      tooltip: {
        formatter: args => `
                  query: ${args.seriesName}
                  <br> avgTime: ${Number(args.data.value).toFixed(3)} ms
                  <br> stdDeviation: ${Number(args.data.stdDeviation).toFixed(3)} ms
                  <br> repetitions: ${args.data.repetitions}`,
      },
    };
  });

  if (chartData.length) {
    const xData = chartData[0].times.map(x => ({
      value: x.commit.substring(0, 15),
      commit: x.commit,
    }));

    return {
      tooltip: {
        show: true,
        trigger: 'item',
      },
      legend: {
        type: 'scroll',
        orient: 'horizontal',
        left: 10,
        bottom: 0,
        data: Object.values(legendsData).sort(),
        tooltip: {
          show: true,
          showDelay: 500,
          triggerOn: 'mousemove',
          formatter: args => Object.keys(legendsData).filter(
            x => legendsData[x] === args.name,
          ),
        },
      },
      calculable: true,
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: xData,
          triggerEvent: true,
        },
      ],
      yAxis: [
        {
          type: 'value',
          axisLabel: {
            formatter: '{value} ms',
          },
        },
      ],
      series,
      dataZoom: [
        {
          type: 'inside',
          zoomOnMouseWheel: 'ctrl',
          filterMode: 'none',
          orient: 'vertical',
        },
      ],
      grid: {
        left: 70,
        top: 20,
        right: 70,
        bottom: 70,
      },
    };
  }
  return {};
};

/* eslint-disable no-plusplus */
export const getLegendsData = (queries) => {
  let matchQuery = 0;
  // const matchInsertQuery = 0;
  let insertQuery = 0;
  // const computeQuery = 0;
  // const otherQuery = 0;
  const queriesMap = {};
  queries.forEach((query) => {
    let value;
    // if (query.includes('compute')) {
    //   value = `computeQuery${++computeQuery}`;
    // } else if (query.includes('insert') && query.includes('match')) {
    // value = `matchInsertQuery${++matchInsertQuery}`;
    /* } else */
    if (query.includes('insert')) {
      value = `insertQuery${++insertQuery}`;
    } else if (query.includes('match')) {
      value = `matchQuery${++matchQuery}`;
    }
    // else {
    //   value = `query${++otherQuery}`;
    // }
    queriesMap[query] = value;
  });
  return queriesMap;
};

const computeAvgTime = spans => spans.reduce((a, b) => a + b.duration, 0) / spans.length;

const computeStdDeviation = (spans, avgTime) => {
  const sum = spans
    .map(span => (span.duration - avgTime) ** 2)
    .reduce((a, b) => a + b, 0);
  return Math.sqrt(sum / spans.length);
};

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
export const getChartData = (queries, querySpans, executions, selectedScale) => queries.map((query) => {
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
