package ca.phon.session.impl;

import java.util.concurrent.atomic.AtomicReference;

import ca.phon.session.Transcriber;

public class TranscriberImpl implements Transcriber {
	
	private final AtomicReference<String> usernameRef = 
			new AtomicReference<String>();
	
	private final AtomicReference<String> nameRef =
			new AtomicReference<String>();
	
	private final AtomicReference<String> passwordRef =
			new AtomicReference<String>();
	
	private volatile boolean usePassword = false;

	@Override
	public String getUsername() {
		return usernameRef.get();
	}

	@Override
	public void setUsername(String username) {
		usernameRef.getAndSet(username);
	}

	@Override
	public String getRealName() {
		return nameRef.get();
	}

	@Override
	public void setRealName(String name) {
		nameRef.getAndSet(name);
	}

	@Override
	public boolean usePassword() {
		return usePassword;
	}

	@Override
	public void setUsePassword(boolean v) {
		usePassword = v;
	}

	@Override
	public String getPassword() {
		return passwordRef.get();
	}

	@Override
	public void setPassword(String password) {
		passwordRef.getAndSet(password);
	}

}
