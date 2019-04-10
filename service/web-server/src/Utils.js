const { spawn } = require('child_process');

function displayStream(stream){
    return new Promise((resolve, reject) => {
        stream.stdout.on('data', (data) => {
            console.log(`${data}`);
        });
          
        stream.stderr.on('data', (data) => {
            process.stdout.write(`${data}`);
        });
          
        stream.on('close', (code) => {
            if(code !== 0){
                console.error(`Script terminated with code ${code}`);
            }
        });
    })
   
}
    
module.exports = {
    parseMergedPR(req){
        return {
            id : req.body.pull_request.merge_commit_sha + Date.now(),
            commit: req.body.pull_request.merge_commit_sha,
            repoUrl: req.body.repository.html_url,
            prMergedAt: req.body.pull_request.merged_at,
            prUrl: req.body.pull_request.html_url,
            prNumber: req.body.pull_request.number,
            executionInitialisedAt: new Date().toISOString(),
            status: 'INITIALISING',
            vmName: 'benchmark-executor-'+ req.body.pull_request.merge_commit_sha.trim()
        }
    },
    createExecutionObject(req){
        return {
            id : req.body.commit + Date.now(),
            commit: req.body.commit,
            repoUrl: req.body.repoUrl,
            executionInitialisedAt: new Date().toISOString(),
            status: 'INITIALISING',
            vmName: 'benchmark-executor-'+ req.body.commit.trim()
        } 
    },
    startBenchmarking(scriptPath, execution){
        const ls = spawn('bash', [scriptPath, execution.repoUrl, execution.id, execution.commit, execution.vmName])
        displayStream(ls);  
    },
    deleteInstance(scriptPath, vmName){
        const ls = spawn('bash', [scriptPath, vmName])
        displayStream(ls);  
    }
}
