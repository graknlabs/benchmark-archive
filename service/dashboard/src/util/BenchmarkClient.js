const requestDefaults = {
  // Default options are marked with *
  mode: 'cors', // no-cors, cors, *same-origin
  cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
  credentials: 'same-origin', // include, *same-origin, omit
  headers: { 'Content-Type': 'application/json' },
  redirect: 'follow', // manual, *follow, error
  referrer: 'no-referrer', // no-referrer, *client
};

// throws an error when response's status indicates error (eg: 401)
const checkStatus = (response) => {
  if (response.status >= 200 && response.status < 300) return response;
  const error = new Error(response.statusText);
  error.response = response;
  throw error;
};

function postData(url, data = {}) {
  // Default options are marked with *
  return fetch(url, {
    ...requestDefaults,
    method: 'POST',
    body: JSON.stringify(data), // body data type must match "Content-Type" header
  })
    .then(response => response.json());
}

function getData(url) {
  return fetch(url, {
    ...requestDefaults,
    method: 'GET',
  })
    .then(checkStatus)
    .then(response => response.json());
}

function getSpans(query) {
  return postData('/span/query', { query });
}

function getExecutions(query) {
  return postData('/execution/query', { query });
}

function stopExecution(execution) {
  return postData('/execution/stop', execution);
}

function deleteExecution(execution) {
  return postData('/execution/delete', execution);
}

function triggerExecution(execution) {
  return postData('/execution/new', execution);
}

function getLatestCompletedExecutions(number) {
  return getExecutions(
    `{ executions(status: ["COMPLETED"], orderBy: "prMergedAt", order:"desc", limit: ${number}){ id commit prMergedAt} }`,
  ).then(executions => executions.data.executions);
}

function getExecutionsSpans(executions) {
  return Promise.all(
    executions.map(exec => getSpans(
      `{ executionSpans( executionName: "${
        exec.id
      }"){ id name duration tags { graphType executionName graphScale description configurationName }} }`,
    ).then(res => res.data.executionSpans)),
  ).then(nestedSpans => nestedSpans.flatMap(x => x));
}

function verifyIdentity() {
  return getData('/auth/verify');
}
export default {
  getExecutions,
  stopExecution,
  deleteExecution,
  getSpans,
  getExecutionsSpans,
  getLatestCompletedExecutions,
  triggerExecution,
  verifyIdentity,
};
