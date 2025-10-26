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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class Shortcut extends SystemLauncher {

    public final static class Builder extends SystemLauncher.AbstractBuilder<Builder, Shortcut> {
        private boolean terminal;
        private Map<Locale, Resource[]> icon = new LinkedHashMap<>();
        private boolean autostart;
        private final List<String> categories = new ArrayList<>();
        private final List<String> keywords = new ArrayList<>();
        
        public Builder(String name) {
        	super(name);
        }

        public Builder withTerminal() {
            return withTerminal(true);
        }

        public Builder withTerminal(boolean terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder withCategories(String... categories) {
            return withCategories(Arrays.asList(categories));
        }
        
        public Builder withCategories(Collection<String> categories) {
            this.categories.addAll(categories);
            return this;
        }

        public Builder withKeywords(String... keywords) {
            return withKeywords(Arrays.asList(keywords));
        }
        
        public Builder withKeywords(Collection<String> keywords) {
            this.keywords.addAll(keywords);
            return this;
        }
                
        public Builder withIcons(Locale locale, String... icon) {
            return withIcons(locale, Arrays.asList(icon).stream().map(Resource::ofBundled).toList().toArray(new Resource[0]));
        }
        
        public Builder withIcons(Locale locale, Resource... icon) {
            this.icon.put(locale, icon);
            return this;
        }
        
        public Builder withIcons(Locale locale, List<Resource> icon) {
            this.icon.put(locale, icon.toArray(new Resource[0]));
            return this;
        }
        
        public Builder withIcons(String... icon) {
            return withIcons(Locale.US, icon);
        }
        
        public Builder withIcons(Resource... icon) {
            return withIcons(Locale.US, icon);
        }
        
        public Builder withIcons(List<Resource> icon) {
            return withIcons(Locale.US, icon);
        }
        
        public Builder withAutostart() {
            return withAutostart(true);
        }

        public Builder withAutostart(boolean autostart) {
            this.autostart = autostart;
            return this;
        }
        
        public Shortcut build() {
            return new Shortcut(this);
        }
    }
    private final Map<Locale, Resource[]> icon;
    private final boolean autostart;
    private final List<String> categories;
    private final List<String> keywords;
    private final boolean terminal;

    private Shortcut(Builder bldr) {
    	super(bldr);
        this.autostart = bldr.autostart;
        this.icon = Collections.unmodifiableMap(new LinkedHashMap<>(bldr.icon));
        this.categories = Collections.unmodifiableList(new ArrayList<>(bldr.categories));
        this.keywords = Collections.unmodifiableList(new ArrayList<>(bldr.keywords));
        this.terminal = bldr.terminal;
    }
    
    public boolean terminal() {
        return terminal;
    }

    public Optional<Resource[]> defaultIcon() {
        var def = icon.get(Locale.US);
        if(def == null && icon.size() > 0) {
            def = icon.values().iterator().next();
        }
        return Optional.ofNullable(def);
    }
    
    public Map<Locale, Resource[]> icon() {
        return icon;
    }

    public boolean autostart() {
        return autostart;
    }

    public List<String> categories() {
        return categories;
    }

    public List<String> keywords() {
        return keywords;
    }
    
}
