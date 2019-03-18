package grakn.benchmark.profiler;

public class ProfilerException extends RuntimeException {
    public ProfilerException(String message, Throwable cause) {
        super(message, cause, false, false);
    }
    public ProfilerException(String message){
        super(message, null, false, false);
    }
}
