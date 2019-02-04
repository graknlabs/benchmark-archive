package grakn.benchmark.profiler.generator;

public class DataGeneratorException extends RuntimeException{
    public DataGeneratorException(String message, Throwable cause) {
        super(message, cause, false, true);
    }

    public DataGeneratorException(String message){
        super(message, null, false, true);
    }
}
