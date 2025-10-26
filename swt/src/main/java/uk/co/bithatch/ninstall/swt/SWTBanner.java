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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import uk.co.bithatch.ninstall.lib.Executable;
import uk.co.bithatch.ninstall.lib.ExecutableBuilderDecorator;
import uk.co.bithatch.ninstall.lib.Resource;
import uk.co.bithatch.ninstall.lib.Executable.BundledResource;
import uk.co.bithatch.ninstall.lib.Resource.Source;

public final class SWTBanner implements ExecutableBuilderDecorator {

    public enum Pos {
        TL, T, TR,
        L, C, R,
        BL, B, BR
    }
    
    public enum Size {
        ORIGINAL, STRETCH, ZOOM
    }

    public final static class Builder {
        private String backgroundColor = "#ffffff";
        private Optional<Resource> imageResource = Optional.empty();
        private Pos imagePosition = Pos.C;
        private Size imageSize = Size.ORIGINAL;
        private int height = 64;
        private int border = 8;
        private boolean cacheImages = true;

        public Builder withoutCacheImages() {
        	return withCacheImages(false);
        }
        
        public Builder withCacheImages(boolean cacheImages) {
        	this.cacheImages =cacheImages;
        	return this;
        }
        
        public Builder withBorder(int border) {
            this.border = border;
            return this;
        }
        
        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }
        
        public Builder withImagePosition(Pos imagePosition) {
            this.imagePosition = imagePosition;
            return this;
        }
        
        public Builder withImageSize(Size imageSize) {
            this.imageSize = imageSize;
            return this;
        }
        
        public Builder withBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder withImageResource(Resource imageResource) {
            return withImageResource(Optional.of(imageResource));
        }
        
        public Builder withImageResource(Optional<Resource> imageResource) {
            this.imageResource = imageResource;
            return this;
        }
        
        public SWTBanner build() {
            return new SWTBanner(this);
        }
    }
    private final String backgroundColor;
    private final Optional<Resource> imageResource;
    private final Pos imagePosition;
    private final Size imageSize;
    private final int height;
    private final int border;
    private final boolean cacheImages;
    
    private ImageData idata; 
    
    private SWTBanner(Builder builder) {
        this.backgroundColor = builder.backgroundColor;
        this.imageResource = builder.imageResource;
        this.imagePosition = builder.imagePosition;
        this.imageSize = builder.imageSize;
        this.height = builder.height;
        this.border = builder.border;
        this.cacheImages = builder.cacheImages;
    }

    @Override
	public void decorateExecutable(Executable.Builder bldr) {
    	
    	// TODO
//    	imageResource.ifPresent(r -> {
//   			bldr.withResource(r);
//    	});
	}

	public Control create(Composite parent) {
        var canvas = new Canvas(parent, SWT.NONE);

        imageResource.ifPresent(res -> {
            ImageData id = idata;
            
            if(id == null) {
	            try(var in = res.in()) {
	                id = new ImageData(in);
	                if(cacheImages) {
	                	idata = id;
	                }
	            }
	            catch(IOException ioe) {
	                throw new UncheckedIOException(ioe);
	            }
            }
            var image = new Image(parent.getDisplay(), id);
            canvas.addListener(SWT.Paint, e -> {
                var sz = image.getBounds();
                
                var spaceWidth = canvas.getBounds().width - (border * 2);
                var spaceHeight = canvas.getBounds().height - (border * 2);

                var tsz = new Rectangle(border, border, sz.width, sz.height);
                
                switch(imageSize) {
                case STRETCH:
                    tsz.width = spaceWidth;
                    tsz.height = spaceHeight;
                    break;
                case ZOOM:
                    tsz.height = spaceHeight;
                    tsz.width = (sz.width * tsz.height) / sz.height;
                    if (tsz.width > spaceWidth)
                     {
                       tsz.width = spaceWidth;
                       tsz.height = (sz.height * tsz.width) / sz.width;
                     }
                default:
                    break;
                }

                /* X */
                switch(imagePosition) {
                case T:
                case C:
                case B:
                    tsz.x = ( canvas.getBounds().width - tsz.width) / 2;
                    break;
                default:
                    break;
                }
                switch(imagePosition) {
                case TR:
                case R:
                case BR:
                    tsz.x = ( canvas.getBounds().width - tsz.width - border);
                    break;
                default:
                    break;
                }
                
                /* Y */
                switch(imagePosition) {
                case L:
                case C:
                case R:
                    tsz.y = ( canvas.getBounds().height - tsz.height) / 2;
                    break;
                default:
                    break;
                }
                switch(imagePosition) {
                case BL:
                case B:
                case BR:
                    tsz.y = ( canvas.getBounds().height - tsz.height - border);
                    break;
                default:
                    break;
                }
                
                e.gc.drawImage(image, 0, 0, sz.width, sz.height, tsz.x, tsz.y, tsz.width, tsz.height);
            }); 
        });
        canvas.setBackground(SWTColor.decode(parent.getDisplay(), backgroundColor));
        return canvas;
    }

    public int height() {
        return height;
    }
}
