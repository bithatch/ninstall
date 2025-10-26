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

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import com.sshtools.liftlib.Elevator;
import com.sshtools.liftlib.IElevator;
import com.sshtools.tinytemplate.Templates.TemplateModel;
import com.sshtools.tinytemplate.Templates.TemplateProcessor;

import uk.co.bithatch.ninstall.lib.installer.InstallerAttribute;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallerAttribute;


public abstract class SetupApp<CTX extends SetupAppContext<?, ?>, TK extends SetupAppToolkit<CTX>> 
	implements ExecutableBuilderDecorator {

    @SuppressWarnings("unchecked")
    public abstract static class Builder<APP extends SetupApp<CTX, ?>, BLDR extends Builder<APP, BLDR, CTX>, CTX extends SetupAppContext<?, ?>> {
        private List<SetupPage> pages = new ArrayList<>();
        private List<SetupAppOptions> options = new ArrayList<>();
        private Optional<UUID> appId = Optional.empty();
//        private Optional<List<String>> arguments = Optional.empty();
        private Optional<DisplayMode> displayMode = Optional.empty();
        private Optional<Mode> mode = Optional.empty();
        private Optional<IElevator> elevator = Optional.empty();

        public BLDR withElevator(IElevator elevator) {
            this.elevator = Optional.of(elevator);
            return (BLDR)this;
        }
        
        public BLDR withMode(Mode mode) {
            this.mode = Optional.of(mode);
            return (BLDR)this;
        }
        
        public BLDR withDisplayMode(DisplayMode displayMode) {
            this.displayMode = Optional.of(displayMode);
            return (BLDR)this;
        }

//        public BLDR withArguments(String... arguments) {
//            return withArguments(Arrays.asList(arguments));
//        }
//        
//        public BLDR withArguments(List<String> arguments) {
//            this.arguments = Optional.of(arguments);
//            return (BLDR)this;
//        }
        
        public BLDR withAppId(UUID appId) {
            this.appId = Optional.of(appId);
            return (BLDR)this;
        }
        
        public BLDR withHumanReadableAppId(String appId) {
            try {
                return withAppId(UUID.nameUUIDFromBytes(appId.getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        
        public BLDR withOptions(SetupAppOptions... options) {
            return withOptions(Arrays.asList(options));
        }
        
        public BLDR withOptions(Collection<SetupAppOptions> options) {
            this.options.addAll(options);
            return (BLDR)this;
        }
        
        public BLDR withPage(SetupPage... pages) {
            return withPage(Arrays.asList(pages));
        }

        public BLDR withPage(Collection<SetupPage> pages) {
            this.pages.addAll(pages);
            return (BLDR)this;
        }

        public abstract APP build();
    }

    private final List<SetupPage> pages;
    private final List<SetupAppOptions> options;
    private final UUID appId;
    private final Class<? extends TK> toolkitClass;
    private final Optional<DisplayMode> defaultDisplayMode;
    private final TemplateProcessor processor;
	private final Optional<Mode> defaultMode;
    private final Optional<IElevator> elevator;
    
    private  CTX context;

    protected SetupApp(Builder<?,?, CTX> bldr, Class<TK> toolkitClass) {
        this.defaultDisplayMode = bldr.displayMode;
        this.defaultMode = bldr.mode;
        this.toolkitClass = toolkitClass;
        this.pages = Collections.unmodifiableList(new ArrayList<>(bldr.pages));
        this.options = Collections.unmodifiableList(new ArrayList<>(bldr.options));
        this.appId = bldr.appId.orElseThrow(() -> new IllegalStateException("An application ID must be set. This should ideal never change throughout the life of your application. You can use withAppId() to specifiy and exact ID, or use the recommended withHumanReadableAppId() to generate a UUID from a phrase."));
        this.elevator = bldr.elevator;
        processor = new TemplateProcessor.Builder().
                build();
    }
    
    public final UUID appId() {
        return appId;
    }
    
    public final List<SetupPage> pages() {
        return pages;
    }
    
    /* TODO change to variadic */
    public final InstallResult setup(String[] args) {
        var scope = Scope.current();
        var registry = new Registry(scope);

        var arglist = Arrays.asList(args);
        var displayMode = calcDisplayMode(arglist).or(() -> defaultDisplayMode);
        var toolkit = getBestSetupAppToolkit(displayMode);
        var mode = arglist.isEmpty() || !arglist.contains("--unattended")? this.defaultMode.orElse(Mode.INTERACTIVE) : Mode.UNATTENDED; 
        
        var opts = options.stream().filter(o -> o.root().isAssignableFrom(toolkit.getClass())).findFirst().orElse(toolkit.defaultOptions());
        context = createContext(scope, registry, opts, mode);
        beforeSetup();
        var result = InstallResult.CANCELLED;
        try {
            result = toolkit.setup(context);
            afterSetup(result);
            return result;
        }
        finally {
            alwaysAfterSetup(result);
        }
    }
    
    @Override
	public final void decorateExecutable(Executable.Builder bldr) {
    	beforeDecorateExecutable(bldr);
    	options.forEach(o -> o.decorateExecutable(bldr));
    	pages.forEach(p -> p.decorateExecutable(bldr));
    	afterDecorateExecutable(bldr);
	}
    
    public IElevator elevator() {
    	return this.elevator.orElseGet(Elevator::elevator);
    }
    
    protected void beforeDecorateExecutable(Executable.Builder bldr) {
    }
    
    protected void afterDecorateExecutable(Executable.Builder bldr) {
    }

	private Optional<DisplayMode> calcDisplayMode(List<String> args) {
        if(args.contains("--console") || args.contains("-c")) {
            return Optional.of(DisplayMode.CONSOLE);
        }
        else if(args.contains("--graphical") || args.contains("-g")) {
            return Optional.of(DisplayMode.GRAPHICAL);
        }
        else
            return Optional.empty();
    }
    

    public String processString(String str) {
        /* TODO tinytemplate needs a better way to map to maps  */
        var model = TemplateModel.ofContent(str);
        context.attributes().forEach((k,v) -> {
            if(k instanceof AppAttribute) {
                model.variable("app." + k.name(), v);
            }
            else if(k instanceof InstallerAttribute) {
                model.variable("installer." + k.name(), v);
            }
            else if(k instanceof UninstallerAttribute) {
                model.variable("uninstaller." + k.name(), v);
            }
            else {
                model.variable("attr." + k.name(), v);   
            }
        });
        return processor.process(model);
    }
    
    public abstract String name();
    
    protected CTX context() {
        return context;
    }
    
    protected void beforeSetup() {
    }
    
    protected void afterSetup(InstallResult result) {
    }
    
    protected void alwaysAfterSetup(InstallResult result) {
    }

    protected abstract CTX createContext(Scope scope, Registry registry, SetupAppOptions options, Mode mode);

    protected TK getBestSetupAppToolkit(Optional<DisplayMode> displayMode) {
        TK first = null, best = null;
        for(var installer : ServiceLoader.load(toolkitClass)) {
            if(installer.isValid()) {
                if(first == null) {
                    first = installer;
                }
                if(displayMode.isPresent() && displayMode.get().equals(installer.mode())) {
                    best = installer;
                    break;
                }
                else if(installer.isBest() && best == null) {
                    best = installer;
                }
            }
        }
        if(best == null) 
            best = first;
        if(best == null)
            throw new IllegalStateException(MessageFormat.format("No setup toolkits found for ''{0}''. It is likely libraries were not included when building the setup tools (installer, uninstaller or update). Try adding mvnpkg-swt and/or mvnppkg-console to your project.", toolkitClass.getName()));
        return best;
    }


}
