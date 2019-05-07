import Vue from 'vue';

Vue.filter('parseDate', (ISOdate) => {
  if (!ISOdate) return 'N/A';
  const epoch = Date.parse(ISOdate);
  return new Date(epoch).toLocaleString('en-GB', { hour12: false });
});

Vue.filter('replaceBlank', (str, replacement) => {
  if (str === '' || str === null) return replacement;
  return str;
});

Vue.filter('truncate', (str, limit) => {
  if (str.length < limit) return str;
  return `${str.substring(0, limit)}...`;
});
