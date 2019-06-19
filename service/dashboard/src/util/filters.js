import Vue from 'vue';

Vue.filter('parseDate', (ISOdate) => {
  if (!ISOdate) return 'N/A';
  const epoch = Date.parse(ISOdate);
  return new Date(epoch).toLocaleString('en-GB', {
    hour12: false, day: 'numeric', month: 'short', year: '2-digit', hour: 'numeric', minute: 'numeric',
  });
});

Vue.filter('truncate', (str, limit) => {
  if (str.length < limit) return str;
  return `${str.substring(0, limit)}...`;
});
