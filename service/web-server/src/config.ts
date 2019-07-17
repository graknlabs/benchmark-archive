export default {
  es: {
    host: (process.env.NODE_ENV === 'production' ? 'localhost' : 'http://35.237.252.2'),
    port: 9200,
  },
  web: {
    port: {
      http: 80,
      https: 443,
    },
  },
  auth: {
    intervalInMinutesToFetchGraknLabsMembers: 10,
  },
};
