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

import java.text.MessageFormat;

public interface Progress {
    
    public final static class DefaultProgress implements Progress {

        private double progress;
		private double total;
		private float weight = 1;

		@Override
        public void error(String text, Throwable exception, Object... args) {
            printmsg("!", text, args);
            if(exception != null) {
                exception.printStackTrace();
            }
        }

        @Override
        public void warning(String text, Object... args) {
            printmsg("+", text, args);            
        }

        @Override
        public void info(String text, Object... args) {
            printmsg("-", text, args);                        
        }

        @Override
        public void alert(String text, Object... args) {
            printmsg("*", text, args);                             
        }

        @Override
        public void command(String text, Object... args) {
            printmsg("$", text, args);            
        }

        private void printmsg(String marker, String text, Object... args) {
            if(args.length == 0)
                System.out.format("[%s] %s%n", marker, text);
            else 
                System.out.format("[%s] %s%n", marker, MessageFormat.format(text, args));
        }

		@Override
		public Progress parent() {
			throw new UnsupportedOperationException("The default progress has no parent.");
		}

		@Override
		public void total(double total) {
			this.total = total;
		}

		@Override
		public void progressed(double progress) {
	        this.progress = Math.min(total, progress);
		}

		@Override
		public double total() {
			return total;
		}

		@Override
		public double progressed() {
			return progress;
		}

		@Override
		public void reset() {
			total = progress = 0;
		}

		@Override
		public void weight(float weight) {
			this.weight  = weight;
		}

		@Override
		public float weight() {
			return weight;
		}
        
    }
    
    public static Progress defaultProgress() {
        return new DefaultProgress();
    }

    default void error(String text, Object... args) {
        error(text, null, args);
    }

    default void exception(Throwable exception, String text, Object... args) {
        error(text + " " + exception.getMessage(), null, args);
    }
    
    void progressed(double amount);
    
    void error(String text, Throwable exception, Object... args);

    void warning(String text, Object... args);

    void info(String text, Object... args);

    void alert(String text, Object... args);
    
    void command(String text, Object... args);

	void total(double total);
	
	void weight(float weight);
	
	float weight();
    
    Progress parent();
    
    default void adjustTotal(double len) {
    	total(total() + len);
    }
    
	default boolean none() {
		return progressed() == 0;
	}
	
	default void complete() {
		if(total() < 1)
			total(1);
		progressed(total());
	}

	default void step(double amount) {
		progressed(progressed() + amount);			
	}

	default void step() {
		step(1);
	}
	
	void reset();
	
	double progressed();

	double total();
}
