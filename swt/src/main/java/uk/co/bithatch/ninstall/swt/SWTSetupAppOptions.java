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
package uk.co.bithatch.ninstall.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import uk.co.bithatch.ninstall.lib.Resource;
import uk.co.bithatch.ninstall.lib.SetupAppOptions;

public final class SWTSetupAppOptions implements SetupAppOptions {
    
    public final static class Builder {
        private int width = 640;
        private int height = 480;
        private Optional<SWTBanner> banner = Optional.empty();
        private List<Resource> icons = new ArrayList<>();

        public Builder withIcons(Resource... icons) {
            return withIcons(Arrays.asList(icons));
        }
        
        public Builder withIcons(Collection<Resource> icons) {
            this.icons.addAll(icons);
            return this;
        }

        public Builder withBanner(SWTBanner banner) {
            this.banner = Optional.of(banner);
            return this;
        }
        
        public Builder withSize(int width, int height) {
            return withWidth(width).withHeight(height);
        }
        
        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }
        
        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }
        
        public SWTSetupAppOptions build() {
            return new SWTSetupAppOptions(this);
        }
    }
    
    public final static class Default {
        private final static SWTSetupAppOptions DEFAULT = new Builder().build();
    }
    
    private final int width;
    private final int height;
    private final Optional<SWTBanner> banner;
    private final List<Resource> icons;

    
    private SWTSetupAppOptions(Builder bldr) {
        this.width = bldr.width;
        this.height = bldr.height;
        this.banner = bldr.banner;
        this.icons = Collections.unmodifiableList(new ArrayList<>(bldr.icons));
    }
    
    public List<Resource> icons() {
        return icons;
    }
    
    public Optional<SWTBanner> banner() {
        return banner;
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }

    @Override
    public Class<?> root() {
        return SWTSetupToolkit.class;
    }

    public static SWTSetupAppOptions defaultOptions() {
        return Default.DEFAULT;
    }
}
