/*
 * Copyright © 2020 Bithatch (brett@bithatch.co.uk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the “Software”), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package uk.co.bithatch.ninstall.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Manifest {

    public final static class Builder {
        private final List<OutputFileset> output = new ArrayList<>();
        private final String name;
        private final String version;
        
        public Builder(String name, String version) {
            this.name = name;
            this.version = version;
        }
        
        public Builder withOutput(OutputFileset... input) {
            return withOutput(Arrays.asList(input));
        }
        
        public Builder withOutput(Collection<OutputFileset> output) {
            this.output.addAll(output);
            return this;
        }
        
        public Manifest build() {
            return new Manifest(this);
        }
    }
    
    private final String name;
    private final String version;
    private final List<OutputFileset> output;
    
    private Manifest(Builder bldr) {
        this.name = bldr.name;
        this.version = bldr.version;
        
        this.output = Collections.unmodifiableList(new ArrayList<>(bldr.output));
    }
    
    public List<OutputFileset> output() {
        return output;
    }

    public String name() {
        return name;
    }
    
    public String version() {
        return version;
    }
}
