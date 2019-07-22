module.exports = {
    root: true,
    env: {
        node: true,
    },
    extends: ["tslint-config-airbnb"],
    rules: {
        "max-line-length": [true, 150],
        "variable-name": {
            options: ['allow-pascal-case']
        },
        "object-literal-sort-keys": false,
    },
};
