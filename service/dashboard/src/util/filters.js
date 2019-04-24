import Vue from 'vue';

Vue.filter('parseDate', (ISOdate) => {
  if (!ISOdate) return 'N/A';
  const epoch = Date.parse(ISOdate);
  return new Date(epoch).toLocaleString('en-GB', { hour12: false });
});