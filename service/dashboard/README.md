# Benchmark Dashboard

## Install Prerequisits

### Node JS
Visit the [NodeJS](https://nodejs.org/en/) website and download and install the LTS version. Run the following commands to ensure you have the latest version of `node` and `npm` installed.

```shell
$ node -v
$ npm -v
```

### Yarn
Install [Yarn](https://yarnpkg.com/) globally by running the following command as _superuser_.

```shell
$ npm i -g yarn
```

### Dependencies
```shell
$ yarn install
```

## Get Started
Before serving the dashboard, [start the Web Server](../web-server#start-the-web-server).

### Clone
```shell
$ git clone git@github.com:graknlabs/benchmark.git
$ cd benchmark/service/dashboard
```

#### Set Up Environment Variables
Get in touch with the team to obtain a copy of `.env` file and place it at `dashboard/`.

### Compile and Serve With Hot-reload (for development)
```shell
$ yarn serve
```

### Compile and Minify (for production)
```shell
$ yarn build
```

### Apply Lint
```shell
$ yarn lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).

## Deploy
While inside `dashboard/`, run:

```shell
$ ../deploy_dashboard.sh
```
