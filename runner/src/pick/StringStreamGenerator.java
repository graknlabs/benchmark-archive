package grakn.benchmark.runner.pick;


import grakn.core.client.Grakn;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

/**
 * adapted from https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 *
 * TODO this has substantial overlap in responsibility with CountingStreamGenerator ie. StreamInterface<T>
 */
public class StringStreamGenerator implements StreamInterface<String> {

    /**
     * Generate a random string.
     */
    public String next() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    @Override
    public Stream<String> getStream(Grakn.Transaction tx) {
        return Stream.generate(() -> this.next());
    }

    @Override
    public boolean checkAvailable(int rqeuiredLength, Grakn.Transaction tx) {
        return true;
    }


    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";

    public static final String alphanum = upper + lower + digits;

    private final Random random;

    private final char[] symbols;

    private final char[] buf;

    public StringStreamGenerator(Random random, int stringLength, String symbols) {
        if (stringLength < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = symbols.toCharArray();
        this.buf = new char[stringLength];
    }

    /**
     * Create an alphanumeric string generator.
     */
    public StringStreamGenerator(Random random, int stringLength) {
        this(random, stringLength, alphanum);
    }

}