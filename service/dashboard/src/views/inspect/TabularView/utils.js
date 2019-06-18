const getQueryCardChartOptions = (querySpans) => {
  const queryCardChartOptions = {
    tooltip: {
      show: true,
      trigger: 'item',
    },
    xAxis: {
      type: 'category',
      name: 'Time (ms)',
      nameLocation: 'middle',
      nameTextStyle: {
        padding: [10, 0, 0, 0],
      },
      axisLabel: {
        fontSize: 11,
      },
      data: [],
    },
    yAxis: {
      type: 'value',
      name: 'Occurr.',
      nameRotate: 90,
      nameLocation: 'middle',
      nameTextStyle: {
        padding: [0, 0, 7, 0],
      },
      splitNumber: 2,
      interval: 1,
      axisLabel: {
        fontSize: 11,
      },
    },
    series: [
      {
        data: [],
        type: 'bar',
        barWidth: 20,
        barCategoryGap: '10%',
        tooltip: {
          formatter: args => `${args.data.spans
            .sort((a, b) => (a.duration > b.duration ? 1 : -1))
            .map(span => `Rep ${span.rep + 1}: ${span.duration / 1000} ms`).join('<br>')}`,
        },
      },
    ],
    grid: {
      left: 50,
      top: 30,
      right: 10,
      bottom: 40,
    },
  };


  let sortedSpansExceptFirst = querySpans;
  sortedSpansExceptFirst.sort((a, b) => (a.duration > b.duration ? 1 : -1));
  sortedSpansExceptFirst = sortedSpansExceptFirst.slice(0, -1);

  const numOfBuckets = 4;
  const minSpan = sortedSpansExceptFirst[0];
  const minDuration = Math.floor(minSpan.duration / 1000);
  const maxSpan = sortedSpansExceptFirst[sortedSpansExceptFirst.length - 1];
  const maxDuration = Math.ceil(maxSpan.duration / 1000);
  const minWidth = (maxDuration - minDuration) / numOfBuckets;
  const bins = [];
  let binCount = 0;
  const interval = minWidth;

  // Setup Bins
  for (let i = minDuration; i < maxDuration; i += interval) {
    bins.push({
      binNum: binCount,
      minNum: i,
      maxNum: i + interval,
      count: 0,
      spans: [],
    });
    binCount += 1;
  }

  // Loop through data and add to bin's count
  for (let m = 0; m < sortedSpansExceptFirst.length; m += 1) {
    const span = sortedSpansExceptFirst[m];
    const duration = span.duration / 1000;
    for (let j = 0; j < bins.length; j += 1) {
      const bin = bins[j];
      if (duration > bin.minNum && duration <= bin.maxNum) {
        bin.count += 1;
        bin.spans.push(span);
      }
    }
  }

  const xData = [];
  const seriesData = [];

  for (let n = 0; n < bins.length; n += 1) {
    if (n === bins.lengt - 1) {
      xData.push(bins[n].maxNum);
    } else {
      xData.push(bins[n].minNum);
    }
    seriesData.push({
      value: bins[n].count,
      spans: bins[n].spans,
    });
  }

  queryCardChartOptions.series[0].data = seriesData;
  queryCardChartOptions.xAxis.data = xData;

  return queryCardChartOptions;
};


export default {
  getQueryCardChartOptions,
};
