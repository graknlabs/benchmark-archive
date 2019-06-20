const getQueryCardChartOptions = (spans) => {
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
      binWidth: 1,
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

  // dynamic calculation of number of bins is a difficult problem. no formula guarantees the "right" number of bins, specially when
  // dealing with small datasets. we may need to readjust this (hard-coded) as we increase the number of repetitions of queries, or
  // perhaps, allow the user to chose it as a parameter of the histogram.
  const numOfBins = 4;
  const minDuration = Math.floor(spans[0].duration / 1000);
  const maxDuration = Math.ceil(spans[spans.length - 1].duration / 1000);
  const binWidth = (maxDuration - minDuration) / numOfBins;
  const bins = [];
  let binCount = 0;

  // set up bins
  for (let minNum = minDuration; minNum < maxDuration; minNum += binWidth) {
    bins.push({
      binNum: binCount,
      minNum,
      maxNum: minNum + binWidth,
      // to be populated next
      count: 0,
      spans: [],
    });
    binCount += 1;
  }

  // populate each bin's count and spans
  for (let i = 0; i < spans.length; i += 1) {
    const span = spans[i];
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

  // populate xData and seriesData
  for (let k = 0; k < bins.length; k += 1) {
    if (k === bins.length - 1) {
      xData.push(bins[k].maxNum);
    } else {
      xData.push(bins[k].minNum);
    }
    seriesData.push({
      value: bins[k].count,
      spans: bins[k].spans,
    });
  }

  // complete the queryCardChartOptions
  queryCardChartOptions.series[0].data = seriesData;
  queryCardChartOptions.xAxis.data = xData;

  return queryCardChartOptions;
};


export default {
  getQueryCardChartOptions,
};
