module.exports = {
  root: true,

  env: {
    node: true,
  },

  parserOptions: {
    parser: 'babel-eslint',
  },

  plugins: [
    'vue',
  ],

  extends: [
    'airbnb-base',
    'plugin:vue/recommended',
  ],

  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'prefer-destructuring': ['error', { object: true, array: false }],
    'max-len': ['error', { ignoreComments: true, code: 150 }],
    'no-use-before-define': 0,
    'import/no-unresolved': 0,
    'no-param-reassign': ['error', { props: false }],
    'import/prefer-default-export': 0,
  },
};
