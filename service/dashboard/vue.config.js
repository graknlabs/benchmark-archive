module.exports = {
  devServer: {
    proxy: 'http://127.0.0.1',
  },
  lintOnSave: true,
  css: {
    loaderOptions: {
      sass: {
        data: '@import "@/assets/css/utilities/_index.scss";',
      },
    },
  },
};
