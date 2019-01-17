package grakn.benchmark.runner.exception;

public class DataGeneratorException extends RuntimeException{
    public DataGeneratorException(String message, Throwable cause) {
        super(message, cause, false, false);
    }

    public DataGeneratorException(String message){
        super(message, null, false, false);
    }
}
