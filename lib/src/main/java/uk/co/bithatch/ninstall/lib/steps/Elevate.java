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
package uk.co.bithatch.ninstall.lib.steps;

import java.util.List;

import com.sshtools.liftlib.IElevator;

import uk.co.bithatch.ninstall.lib.ElevatableStep;
import uk.co.bithatch.ninstall.lib.Journals;
import uk.co.bithatch.ninstall.lib.Progress;
import uk.co.bithatch.ninstall.lib.Step;
import uk.co.bithatch.ninstall.lib.StepContext;
import uk.co.bithatch.ninstall.lib.installer.InstallStepContext;
import uk.co.bithatch.ninstall.lib.installer.InstallationContext;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStepContext;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallationContext;
import uk.co.bithatch.ninstall.lib.updater.UpdateContext;
import uk.co.bithatch.ninstall.lib.updater.UpdateStepContext;

public final class Elevate<CTX extends StepContext<?>> implements Step<CTX> {

	private final ElevatableStep<CTX>[] delegates;
	private int completed;

	public Elevate(@SuppressWarnings("unchecked") ElevatableStep<CTX>... delegates) {
		this.delegates = delegates;
	}
	
	@SuppressWarnings("unchecked")
	public Elevate(List<ElevatableStep<CTX>> delegates) {
		this(delegates.toArray(new ElevatableStep[0]));
	}

	@Override
	public void apply(CTX context) throws Exception {
		var ec = elevatedContext(context);
		for(completed = 0; completed < delegates.length; completed++) {
			delegates[completed].apply(ec);
		}
	}

	@Override
	public void rollback(CTX context) throws Exception {
		var ec = elevatedContext(context);
		for(;completed >= 0; completed--) {
			delegates[completed].rollback(ec);
		}
	}

	@Override
	public float init(CTX context) throws Exception {
		var ec = elevatedContext(context);
		var t = 0.0f;
		for(var del : delegates) {
			t += del.init(ec);
		}
		return t;
	}

	@Override
	public void commit(CTX context) throws Exception {
		var ec = elevatedContext(context);
		for(var del : delegates)
			del.commit(elevatedContext(context));
	}

	@SuppressWarnings("unchecked")
	private CTX elevatedContext(CTX context) {
		if (context instanceof InstallStepContext isc) {
			return (CTX) new InstallStepContext() {

				@Override
				public IElevator elevator() {
					return setup().elevator();
				}

				@Override
				public InstallationContext setup() {
					return isc.setup();
				}

				@Override
				public Progress progress() {
					return isc.progress();
				}

				@Override
				public Journals journals() {
					return isc.journals();
				}
			};
		} else if (context instanceof UninstallStepContext usc) {
			return (CTX) new UninstallStepContext() {

				@Override
				public IElevator elevator() {
					return setup().elevator();
				}

				@Override
				public UninstallationContext setup() {
					return usc.setup();
				}

				@Override
				public Progress progress() {
					return usc.progress();
				}

				@Override
				public Journals journals() {
					return usc.journals();
				}
			};
		} else if (context instanceof UpdateStepContext usc) {
			return (CTX) new UpdateStepContext() {

				@Override
				public IElevator elevator() {
					return setup().elevator();
				}

				@Override
				public UpdateContext setup() {
					return usc.setup();
				}

				@Override
				public Progress progress() {
					return usc.progress();
				}

				@Override
				public Journals journals() {
					return usc.journals();
				}
			};
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
