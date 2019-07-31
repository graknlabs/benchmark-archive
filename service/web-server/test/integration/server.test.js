const request = require('supertest');
require('iconv-lite/encodings'); // dont remove: https://stackoverflow.com/questions/49141927/express-body-parser-utf-8-error-in-test
const graphqlHTTP = require('express-graphql');
const utils = require('../../src/Utils');


const executionsControllerMocked = {
    addExecution: jest.fn().mockImplementation(() => Promise.resolve()),
    executionRunning: jest.fn().mockImplementation(() => Promise.resolve()),
    executionCompleted: jest.fn().mockImplementation(() => Promise.resolve()),
    executionFailed: jest.fn().mockImplementation(() => Promise.resolve()),
    queryExecutions: jest.fn().mockImplementation(() => graphqlHTTP({})),
};
mockExecutionController(); // mock it before calling server.js
const app = require('../../src/server');

beforeEach(()=>{ jest.clearAllMocks(); })

describe('/pull_request Tests', () => {
    test("When merged set to false, don't trigger Benchmark", ( done )=>{
        request(app).post('/pull_request')
            .send({
                action: "closed",
                merged: false
            })
            .expect(200)
            .then(res =>{
                expect(res.body.triggered).toBeFalsy();
                done();
            });
    });

    test("When action is not closed, don't trigger Benchmark", ( done )=>{
        request(app).post('/pull_request')
            .send({
                action: "random-action"
            })
            .expect(200)
            .then(res =>{
                expect(res.body.triggered).toBeFalsy();
                done();
            });
    });

    // TODO: fix this to handle the date.now() in extractPRInfo
    test("When PR is closed and merged, trigger Benchmark with correct params", ( done )=>{
        utils.startBenchmarking = jest.fn();
        const executionObject = {
            commit: "1234",
            executionCompletedAt: "",
            prMergedAt: '2018-05-30T20:18:50Z',
            prNumber: "2",
            prUrl: "http://test.com/pulls/2",
            repoId: "0987",
            repoUrl: "http://test.com",
            executionStartedAt: "",
            status: "INITIALISING",
            vmName: "benchmark-executor-1234",
        };
        request(app)
            .post('/pull_request')
            .send({
                action: "closed",
                merged: true,
                pull_request: { merge_commit_sha: "1234", closed_at: "4567"},
                repository: { html_url: "http://test.com", id: "0987" },
            })
            .expect(200)
            .then(res => {
                expect(res.body.triggered).toBeTruthy();
                expect(executionsControllerMocked.addExecution).toBeCalledWith(executionObject);
                expect(startBenchmarking).toBeCalledWith(expect.stringContaining('launch_executor_server.sh'), Object.assign({}, executionObject, {id: "123456789"}));
                done();
            });
    });

});

describe('/execution/start Tests', () => {
    test('When correct request provided, mark execution as STARTED', (done) => {
        request(app).post('/execution/start')
            .send({
                repoId: "4567",
                commit: "1234"
            })
            .expect(200)
            .then(res =>{
                expect(res.body).toEqual({});
                expect(executionsControllerMocked.executionRunning).toBeCalledWith({ repoId: "4567", commit: "1234"});
                done();
            });
    });
});

describe('/execution/completed Tests', () => {
    test('When correct request provided, mark execution as COMPLETED and delete VM', (done) => {
        utils.deleteInstance = jest.fn();
        request(app).post('/execution/completed')
            .send({
                repoId: "4567",
                commit: "1234",
                vmName: "lareginadelcelebrita"
            })
            .expect(200)
            .then(res =>{
                expect(res.body).toEqual({});
                expect(utils.deleteInstance).toBeCalledWith(expect.stringContaining('delete_instance.sh'), "lareginadelcelebrita");
                expect(executionsControllerMocked.executionCompleted).toBeCalledWith({ repoId: "4567", commit: "1234", vmName: "lareginadelcelebrita"});
                done();
            });
    });
});



function mockExecutionController(){
    let execController = require('../../src/ExecutionsController');
    jest.mock('../../src/ExecutionsController');
    execController.mockImplementation(() => executionsControllerMocked);
}

function startBenchmarking (scriptPath, execution) {
    const ls = spawn('bash', [scriptPath, execution.repoUrl, execution.id, execution.commit, execution.vmName]);
    displayStream(ls);
}

function displayStream(stream) {
    return new Promise(() => {
        stream.stdout.on('data', (data) => {
            console.log(`${data}`);
        });

        stream.stderr.on('data', (data) => {
            process.stdout.write(`${data}`);
        });

        stream.on('close', (code) => {
            if (code !== 0) {
                console.error(`Script terminated with code ${code}`);
            }
        });
    });
}