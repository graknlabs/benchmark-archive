# Report Generator

The report generator is a stripped down subset of `profiler`, without Zipkin tracing.

The goal is do high level timing from the client side, in a realistic setting and automatically generate a performance report.

There are two packages:
1. `producer` - contains a script `gcp_report.sh` which spawns client and server machines in GCP.
The client produces a `json` file for each scenario it executes, which are aggregated into a single
json file and pulled back onto the machine running `gcp_report.sh`
2. `formatter` - The raw `json` file from the `producer` can be fed into the `formatter` to produce a formatted
text file report, sorted by query

### Producer
In `producer`, launch with `./gcp_report.sh`, then wait until `report.json` turns up in the same directory and script terminates.

If an error occurs, the `report.json` may actually be a log file from the client.

### Formatter
Run directly with bazel:

```bazel run //report/formatter:report-formatter-binary -- --rawReport /abs/path/to/raw/report.json --destination /abs/path/to/output/folder```