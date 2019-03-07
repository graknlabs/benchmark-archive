package grakn.benchmark.profiler;

public class BootupException extends RuntimeException{
    public BootupException(String message, Throwable cause) {
        super(message, cause, false, false);
    }

    public BootupException(String message){
        super(message, null, false, false);
    }
}
