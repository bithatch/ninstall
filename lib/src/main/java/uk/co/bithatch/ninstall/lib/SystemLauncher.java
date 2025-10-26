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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class SystemLauncher {

    public abstract static class AbstractBuilder<BLDR extends AbstractBuilder<BLDR, SL>, SL extends SystemLauncher> {
        private final String name;
        private Map<Locale, String> displayName = new LinkedHashMap<>();
        private Map<Locale, String> description = new LinkedHashMap<>();
        private Optional<String> command = Optional.empty();
        private Optional<Path> commandPath = Optional.empty();
        private final List<String> arguments = new ArrayList<>();
        
        protected AbstractBuilder(String name) {
            this.name = name;
        }

        public final BLDR withArguments(String... arguments) {
            return withArguments(Arrays.asList(arguments));
        }
        
        @SuppressWarnings("unchecked")
		public final BLDR withArguments(Collection<String> arguments) {
            this.arguments.addAll(arguments);
            return (BLDR)this;
        }

        @SuppressWarnings("unchecked")
		public final BLDR withDisplayName(Locale locale, String displayName) {
            this.displayName.put(locale, displayName);
            return (BLDR)this;
        }
        
        public final BLDR withDisplayName(String displayName) {
            return withDisplayName(Locale.US, displayName);
        }
        
        @SuppressWarnings("unchecked")
		public final BLDR withDescription(Locale locale, String description) {
            this.description.put(locale, description);
            return (BLDR)this;
        }
        
        public final BLDR withDescription(String description) {
            return withDescription(Locale.US, description);
        }
        
        public final BLDR withCommand(String command) {
            return withCommand(Optional.of(command));
        }
        
        @SuppressWarnings("unchecked")
		public final BLDR withCommand(Optional<String> command) {
            this.command = command;
            return (BLDR)this;
        }
        
        public final BLDR withCommandPath(Path commandPath) {
            return withCommandPath(Optional.of(commandPath));
        }
        
        @SuppressWarnings("unchecked")
		public final BLDR withCommandPath(Optional<Path> commandPath) {
            this.commandPath = commandPath;
            return (BLDR)this;
        }

        public abstract SL build();
    }
    private final String name;
    private final Map<Locale, String> displayName;
    private final Map<Locale, String> description;
    private final Optional<String> command;
    private final Optional<Path> commandPath;
    private final List<String> arguments;

    protected SystemLauncher(AbstractBuilder<?,?> bldr) {
        this.commandPath = bldr.commandPath;
        this.name = bldr.name;
        this.displayName = Collections.unmodifiableMap(new LinkedHashMap<>(bldr.displayName));
        this.description  = Collections.unmodifiableMap(new LinkedHashMap<>(bldr.description));
        this.command = bldr.command;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(bldr.arguments));
    }
    
    public final String name() {
        return name;
    }
    
    public final String defaultDisplayName() {
        var def = displayName.get(Locale.US);
        if(def == null && displayName.size() > 0) {
            def = displayName.values().iterator().next();
        }
        if(def == null)
            def = name;
        return def;
    }
    
    public final Optional<String> defaultDescription() {
        var def = description.get(Locale.US);
        if(def == null && description.size() > 0) {
            def = description.values().iterator().next();
        }
        return Optional.ofNullable(def);
    }

    public final Map<Locale, String> displayName() {
        return displayName;
    }

    public final Map<Locale, String> description() {
        return description;
    }

    public final Optional<String> command() {
        return command;
    }

    public final List<String> arguments() {
        return arguments;
    }

    public final Path commandPath() {
        return commandPath.orElseGet(() -> 
            Paths.get(Machine.hostMachine().os().executable(command.orElse(name)))
        );
    }
}
