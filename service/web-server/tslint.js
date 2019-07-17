module.exports = {
    root: true,

    env: {
      node: true,
    },

    extends: [ "tslint-config-airbnb" ],

    rules: {
        "max-line-length": [true, 150],
    },
}