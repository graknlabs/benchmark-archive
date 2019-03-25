/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.generator.provider.value;


import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * adapted from https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 */
public class RandomStringProvider implements ValueProvider<String> {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase(Locale.ROOT);
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUM = UPPER + LOWER + DIGITS;

    private final Random random;
    private final char[] symbols;
    private final char[] buf;

    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * Generate a random string.
     */
    public String next() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    /**
     * Create an alphanumeric string generator.
     */
    public RandomStringProvider(Random random, int stringLength) {
        if (stringLength < 1)
            throw new IllegalArgumentException("Require randomly generated strings to have length > 1");
        this.random = Objects.requireNonNull(random);
        this.symbols = ALPHANUM.toCharArray();
        this.buf = new char[stringLength];
    }

}
