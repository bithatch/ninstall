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

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import uk.co.bithatch.ninstall.lib.StepContext.Journalled;
import uk.co.bithatch.ninstall.lib.StepContext.JournalledCallable;

public class Journals implements Closeable {

    private final Map<String, Journal> journals = new HashMap<>();
	private final SetupAppToolkit<?> toolkit;
    
    public Journals(SetupAppToolkit<?> toolkit) {
    	this.toolkit = toolkit;
    }

	public <V> V journalledCall(String journalId, JournalledCallable<V> journal) throws Exception {
		var jnl = obtainJournal(journalId);
		
		return journal.journalled(jnl.stash(), jnl);
	}
	
	public Collection<Journal> journals() {
		return journals.values();
	}
	
	public void journalled(String journalId, Journalled journal) throws Exception {
		var jnl = obtainJournal(journalId);
		journal.journalled(jnl.stash(), jnl);
		
	}
    
	public void uncheckedJournalled(String journalId, Journalled journal) {
    	try {
    		journalled(journalId, journal);
    	}
    	catch(IOException ioe) {
    		throw new UncheckedIOException(ioe);
    	}
    	catch(RuntimeException re) {
    		throw re;
    	}
    	catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
	public <V> V uncheckedJournalledCall(String journalId, JournalledCallable<V> journal) {
    	try {
    		return journalledCall(journalId, journal);
    	}
    	catch(IOException ioe) {
    		throw new UncheckedIOException(ioe);
    	}
    	catch(RuntimeException re) {
    		throw re;
    	}
    	catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }
	
	public Journal obtainJournal(Path path) {
		return obtainJournal(FilenameUtils.getBaseName(path.getFileName().toString().substring(1)));
	}

	public Journal obtainJournal(String journalId) {
		var jnl = journals.get(journalId);
		if(jnl == null) {
			var stash = new Stash.Builder(toolkit.setupAppContext(), journalId).
	        		build();
			jnl = new Journal.Builder(toolkit.setupAppContext(), journalId).
					withStash(stash).
					onDelete(j -> {
						journals.remove(j.name());
					}).
	        		build();
	        journals.put(journalId, jnl);
		}
		return jnl;
	}

	@Override
	public void close() {
		journals.values().forEach(j -> j.close());
	}
}
