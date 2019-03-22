package grakn.benchmark.report;

public class ReportGeneratorException extends RuntimeException {
    public ReportGeneratorException(String message, Throwable cause) {
        super(message, cause, false, false);
    }
    public ReportGeneratorException(String message){
        super(message, null, false, false);
    }
}
