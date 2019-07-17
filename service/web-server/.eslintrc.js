module.exports = {
    env: {
        node: true,
        es6: true
    },
    extends: [
        'airbnb-base',
        'eslint:recommended',
    ],
    globals: {
        Atomics: 'readonly',
        SharedArrayBuffer: 'readonly'
    },
    parserOptions: {
        parser: 'babel-eslint',
        ecmaVersion: 2018,
        sourceType: 'module'
    },
    rules: {
        'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
        'prefer-destructuring': ['error', { object: true, array: false }],
        'max-len': ['error', { ignoreComments: true, code: 150 }],
        'no-use-before-define': 0,
        'import/no-unresolved': 0,
        'no-param-reassign': ['error', { props: false }],
    }
};