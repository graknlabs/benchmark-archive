# Benchmark Web Server

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

### Clone
```shell
$ git clone git@github.com:graknlabs/benchmark.git
$ cd benchmark/service/web-server
```

#### Set Up Environment Variables
Get in touch with the team to obtain a copy of `.env` file and place it at `web-server/`.

### Start the Web Server
```shell
$ NODE_ENV=development yarn start
```

## Deploy
While inside `web-server/`, run:

```shell
$ ../deploy_web_server.sh
```

## Stop the Production Web Server
```shell
gcloud compute ssh ubuntu@benchmark-service --zone=us-east1-b --command='sudo pkill node'
```

## Start the Production Web Server
```shell
gcloud compute ssh ubuntu@benchmark-service --zone=us-east1-b --command='cd ~/service/web-server && nohup sudo NODE_ENV=production yarn start 2>&1 | tee -a ~/logs/node_server.log &'

```
