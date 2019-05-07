
function processExecutionQueries(spans, queries) {
  /**
   * Step 1:
   *    Input: spans
   *    Output: array of objects containing graphName + Scale info, in the following format
   *    {
   *      type: "",
   *      scales: [
   *        {
   *          value: 2000,
   *          span: {
   *            id: "",
   *            duration: 1111
   *          },
   *          queries: [] // this is filled in, in the next step
   *        }
   *      ]
   *    }
   */
  const interQueriesSet = [];
  spans.forEach((span) => {
    const { graphType } = span.tags;
    const existsgraphType = interQueriesSet.some(querySet => querySet.type === span.tags.graphType);

    const querySet = {};
    if (!existsgraphType) {
      querySet.type = graphType;
      querySet.scales = [];
      querySet.scales.push({
        value: span.tags.graphScale,
        span: {
          id: span.id,
          duration: span.duration,
        },
        queries: [],
      });
      interQueriesSet.push(querySet);
    } else {
      interQueriesSet.filter(element => element.type === span.tags.graphType)[0].scales.push({
        value: span.tags.graphScale,
        span: {
          id: span.id,
          duration: span.duration,
        },
        queries: [],
      });
    }
  });

  /**
   * Step 2:
   *    Input: queries and interQueriesSet
   *    Output: interQueriesSet + reps/queries info, in the following format
   *    {
   *      graphName: "",
   *      scales: [
   *        {
   *          value: 2000,
   *          span: {
   *            id: "",
   *            duration: 111101
   *          },
   *          queries: [
   *            {
   *              value: 1,
   *              type: "insert",
   *              reps: [
   *                {
   *                  order: 1,
   *                  span: {
   *                    id: "",
   *                    parentId: "",
   *                    duration: 1111
   *                  }
   *                }
   *              ]
   *            }
   *          ] // this is filled in, in the next step
   *        }
   *      ]
   *    }
   */

  queries.forEach((query) => {
    let querySetIndex;
    let scaleIndex;
    let parentId;
    query.forEach((item, index) => {
      if (index === 0) {
        // eslint-disable-next-line prefer-destructuring
        parentId = item.parentId;
        querySetIndex = interQueriesSet.findIndex(querySet => querySet.scales.some(scale => scale.span.id === parentId));
        scaleIndex = interQueriesSet[querySetIndex].scales.findIndex(scale => scale.span.id === parentId);
      }
      const queryValue = item.tags.query;
      const queryRep = {
        order: item.tags.repetition + 1,
        span: {
          id: item.id,
          parentId: item.parentId,
          duration: item.duration,
        },
      };
      const queryItem = {
        value: item.tags.query,
        type: item.tags.type,
        reps: [queryRep],
      };

      const queriesOfInterest = interQueriesSet[querySetIndex].scales[scaleIndex].queries;
      const existsQuery = queriesOfInterest.some(q => q.value === queryValue);
      if (existsQuery) {
        queriesOfInterest.filter(q => q.value === queryValue)[0].reps.push(queryRep);
      } else {
        queriesOfInterest.push(queryItem);
      }
    });
  });

  /**
   * Step 3:
   *    Input: interQueriesSet
   *    Output: finalQueriesSet with the following format
   *    {
   *      graphName: "",
   *      types: [
   *        {
   *          value: "",
   *          scales: [...]
   *        }
   *      ]
   *    }
   */
  const finalQueriesSet = [];
  interQueriesSet.forEach((interQuerySet) => {
    let queryTypes = [...new Set(interQuerySet.scales.map(scale => scale.queries[0].type))];
    queryTypes = queryTypes.map(type => ({ value: type, scales: [] }));
    const finalQuerySet = {
      type: interQuerySet.type,
      queryTypes,
    };

    queryTypes.forEach((type) => {
      const scales = interQuerySet.scales.filter(scale => scale.queries[0].type === type.value);
      const typeIndex = finalQuerySet.queryTypes.findIndex(t => t.value === type.value);
      finalQuerySet.queryTypes[typeIndex].scales = scales;
    });

    finalQueriesSet.push(finalQuerySet);
  });

  return finalQueriesSet;
}

export default {
  processExecutionQueries,
};
