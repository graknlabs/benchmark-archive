function flattenGraphs(graphs) {
  const flattenedGraphs = graphs.map(graph => ({
    id: graph.id,
    type: graph.tags.graphType,
    scale: graph.tags.graphScale,
    executionId: graph.tags.executionName,
  }));
  return flattenedGraphs;
}

function flattenQuerySpans(querySpanGroups) {
  const flattenedSpans = [];
  querySpanGroups.forEach((querySpanGroup) => {
    querySpanGroup.forEach((querySpan) => {
      flattenedSpans.push({
        id: querySpan.id,
        parentId: querySpan.parentId,
        name: querySpan.name,
        value: querySpan.tags.query,
        rep: querySpan.tags.repetition,
        duration: querySpan.duration,
      });
    });
  });
  return flattenedSpans;
}

function addExecutionIdAndScaleToQuerySpan(graphs, querySpans) {
  const querySpansWithExecutionAndScale = querySpans.map((querySpan) => {
    const { executionId, scale } = graphs.filter(graph => graph.id === querySpan.parentId)[0];
    querySpan.executionId = executionId;
    querySpan.scale = scale;
    return querySpan;
  });
  return querySpansWithExecutionAndScale;
}

export default {
  flattenGraphs,
  flattenQuerySpans,
  addExecutionIdAndScaleToQuerySpan,
};
