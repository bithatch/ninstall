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
package uk.co.bithatch.ninstall.lib.installer.steps;

import static uk.co.bithatch.ninstall.lib.IO.ioRunInTempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.Archives;
import uk.co.bithatch.ninstall.lib.Formatting;
import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.ProgressInputStream;
import uk.co.bithatch.ninstall.lib.SDKMan;
import uk.co.bithatch.ninstall.lib.Archives.Format;
import uk.co.bithatch.ninstall.lib.SDKMan.Content;
import uk.co.bithatch.ninstall.lib.SDKMan.Package;
import uk.co.bithatch.ninstall.lib.SDKMan.Platform;
import uk.co.bithatch.ninstall.lib.installer.InstallStep;
import uk.co.bithatch.ninstall.lib.installer.InstallStepContext;

public final class InstallSDK implements InstallStep {
	
	public final static InstallSDK java() {
		return new Builder().build();
	}
	
	public final static InstallSDK java(String identifier) {
		return new Builder().
				withIdentifier(identifier).
				build();
	}
	
	public final static class Builder {
		
		private String candidate = "java";
		private Optional<String> identifier = Optional.empty();
		private Optional<Platform> platform = Optional.empty();

		public Builder withCandidate(String candidate) {
			this.candidate = candidate;
			return this;
		}

		public Builder withIdentifier(String identifier) {
			this.identifier = Optional.of(identifier);
			return this;
		}

		public Builder withPlatform(Platform platform) {
			this.platform = Optional.of(platform);
			return this;
		}
		
		public InstallSDK build() {
			return new InstallSDK(this);
		}
		
	}

    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(InstallSDK.class.getName());

	private static final String SDKS = "sdks";
    
	private final String candidate;
	private final Optional<String> identifier;
	private final Optional<Platform> platform;
	private final SDKMan sdkman;

	private Package pkg;

    
    private InstallSDK(Builder builder) {
    	this.candidate = builder.candidate;
    	this.identifier = builder.identifier;
    	this.platform = builder.platform;

        sdkman = new SDKMan();
    }

    @Override
    public float init(InstallStepContext context) {
        return 100;
    }

    @Override
    public void apply(InstallStepContext context) throws Exception {
        var progress = context.progress();
        
        pkg = getPackage().orElseThrow(() -> new IOException("No SDK named " + candidate));
        
        var sdkpath = sdkPath(pkg, context);
        
        if(Files.exists(sdkpath)) {
        	progress.info(RESOURCES.getString("alreadyExists"), pkg.displayName());
            return;
        }
    	
        context.journals().journalled(SDKS, (stash, journal) -> {
        
	    	Content archive;
			if(platform.isPresent())
	        	archive = pkg.download(platform.get());
	        else
	        	archive = pkg.download();
	        
			progress.total(archive.contentSize());
	        try(var in = archive.input()) {
	            progress.info(RESOURCES.getString("extracting"), pkg.displayName(), Formatting.toByteSize(archive.contentSize()));
	        	stash.stashOrCreated(sdkpath);
	
	        	ioRunInTempDir(context.setup().installLocation(), archiveTmpdir -> {
	        		stash.created(sdkpath.getParent());
	        		IO.checkDir(sdkpath.getParent());
		            Files.move(
		            	Archives.extract(Format.fromFilename(archive.filename()), new ProgressInputStream(in, progress), archiveTmpdir).get(0), 
		            	sdkpath
		            );
	            	journal.log(context.setup().installLocation().relativize(sdkpath));
	        	});
	            
	        	progress.info(RESOURCES.getString("extracted"), pkg.displayName());
	        }
        });
    }

    @Override
    public void rollback(InstallStepContext context) throws Exception {
    	context.journals().journalled(SDKS, (stash, journal) -> {
            stash.restoreAndClose(context.progress());
    	});
    }

    @Override
    public void commit(InstallStepContext context) throws Exception {
    	context.journals().journalled(SDKS, (stash, journal) -> {
            stash.close();
    	});
    }

	private Optional<? extends Package> getPackage() { 
		if(identifier.isPresent()) {
			if(platform.isPresent()) {
				return sdkman.version(candidate, platform.get(), identifier.get());
			}
			else {
				return sdkman.version(candidate, identifier.get());
			}
		}
		else {
			return sdkman.get(candidate);
		}
	}

	private Path sdkPath(Package sdk, InstallStepContext context) {
		var installLocation = context.setup().installLocation();
        var sdks = installLocation.resolve("sdks");
        var sdkpath = sdks.resolve(sdk.candidate());
		return sdkpath.resolve(sdk.identifier());
	}
}
