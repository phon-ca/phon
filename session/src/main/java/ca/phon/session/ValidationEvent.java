package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

public class ValidationEvent implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(ValidationEvent.class, this);
	
	private Session session;
	
	private int record;
	
	private String tierName;
	
	private int group;
	
	private String message;

	public ValidationEvent(Session session, String message) {
		super();
		this.session = session;
		this.message = message;
	}

	public ValidationEvent(Session session, int record, String message) {
		super();
		this.session = session;
		this.record = record;
		this.message = message;
	}

	public ValidationEvent(Session session, int record, String tierName,
			int group, String message) {
		super();
		this.session = session;
		this.record = record;
		this.tierName = tierName;
		this.group = group;
		this.message = message;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public int getRecord() {
		return record;
	}

	public void setRecord(int record) {
		this.record = record;
	}

	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

}
