"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const child = require("child_process");
class InstanceController {
    constructor(execution) {
        this.execution = execution;
    }
    start() {
        const startScriptPath = `${__dirname}/../../launch_executor_server.sh`;
        const { repoUrl, id, commit, vmName } = this.execution;
        const ls = child.spawn('bash', [startScriptPath, repoUrl, id, commit, vmName]);
        this.displayStream(ls);
    }
    delete() {
        const deleteScriptPath = `${__dirname}/../../delete_instance.sh`;
        const ls = child.spawn('bash', [deleteScriptPath, this.execution.vmName]);
        this.displayStream(ls);
    }
    displayStream(stream) {
        // ???: we're not resolving or rejecting anything here. why are we returning a promise then?
        // eslint-disable-next-line no-unused-vars
        return new Promise((resolve, reject) => {
            stream.stdout.on('data', (data) => {
                console.log(`${data}`);
            });
            stream.stderr.on('data', (data) => {
                process.stdout.write(`${data}`);
            });
            stream.on('close', (code) => {
                if (code !== 0)
                    console.error(`Script terminated with code ${code}`);
            });
        });
    }
}
exports.InstanceController = InstanceController;
