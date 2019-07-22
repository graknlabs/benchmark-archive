"use strict";
// tslint:disable:max-line-length
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const ComputeClient = require("@google-cloud/compute");
const child_process_promise_1 = require("child-process-promise");
const config_1 = require("../config");
class InstanceController {
    constructor(execution) {
        this.start = () => __awaiter(this, void 0, void 0, function* () {
            const { id, repoUrl, commit, vmName } = this.execution;
            const startupScript = `
      #!/bin/bash

      report_failure() {
        curl --header "Content-Type: application/json" \
          --request POST \
          --data "{\"executionId\":\"${id}\" }" \
          --insecure \
          https://${this.esUri}/execution/failed

        exit 1
      }
      # catch any error - report failure to Service and exit the script
      trap report_failure ERR

      # navigate to home
      cd /home/ubuntu

      # build grakn
      git clone ${repoUrl}
      cd grakn
      sudo git checkout ${commit}
      sudo bazel build //:assemble-linux-targz

      # unzip grakn
      cd bazel-genfiles
      sudo tar -xf grakn-core-all-linux.tar.gz

      # start grakn
      # cd grakn-core-all-linux
      # sudo ./grakn server start --benchmark

      # build benchmark
      cd /home/ubuntu
      git clone https://github.com/graknlabs/benchmark.git
      cd benchmark
      bazel build //:profiler-distribution

      # unzip benchmark
      cd bazel-genfiles
      unzip profiler.zip
    `;
            console.log(`Starting the ${vmName} instance`);
            const createVMResp = yield this.gcClient.zone(this.zone).createVM(vmName, {
                machineType: 'n1-standard-16',
                disks: [{
                        boot: true,
                        source: `https://www.googleapis.com/compute/v1/projects/${this.project}/zones/${this.zone}/disks/benchmark-executor`,
                    }],
                metadata: {
                    items: [
                        {
                            key: 'startup-script',
                            value: startupScript,
                        },
                    ],
                },
                networkInterfaces: [{ accessConfigs: [{}] }],
            });
            const operation = createVMResp[1];
            return operation;
        });
        this.delete = () => __awaiter(this, void 0, void 0, function* () {
            const vmName = this.execution.vmName;
            const vm = this.gcClient.zone(this.zone).vm(vmName);
            const [operation] = yield vm.delete();
            yield operation.promise();
            console.log(`${vmName} instance was successfully deleted.`);
        });
        this.runZipkin = (vmName, callback) => {
            console.log('Running Zipkin.');
            const bashFile = `${__dirname}/zipkin.sh`;
            this.executeBashOnVm(bashFile, vmName, callback);
        };
        this.runBenchmark = (vmName, executionId, callback) => {
            console.log('Running benchmark.');
            const bashFile = `${__dirname}/benchmark.sh`;
            this.executeBashOnVm(bashFile, vmName, callback, [executionId, this.esUri]);
        };
        this.executeBashOnVm = (bashFile, vmName, callback, options = []) => __awaiter(this, void 0, void 0, function* () {
            const promise = child_process_promise_1.spawn('bash', [bashFile, process.env.GOOGLE_APPLICATION_CREDENTIALS, vmName, this.zone, ...options]);
            const childProcess = promise.childProcess;
            childProcess.stdout.on('data', (data) => { console.log('[spawn] stdout: ', data.toString()); });
            childProcess.stderr.on('data', (data) => { console.log('[spawn] stderr: ', data.toString()); });
            promise
                .then(() => { callback(); })
                .catch((error) => { console.error('[spawn] ERROR: ', error); });
        });
        this.execution = execution;
        this.gcClient = new ComputeClient();
        this.zone = 'us-east1-b';
        this.project = 'grakn-dev';
        this.esUri = `${config_1.config.es.host}:${config_1.config.es.port}`;
    }
}
exports.InstanceController = InstanceController;
