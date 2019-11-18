package ca.phon.session.spi;

public interface TranscriberSPI {

	/**
	 * The transcriber username
	 */
	public String getUsername();
	
	public void setUsername(String username);
	
	/**
	 * The real name
	 */
	public String getRealName();
	
	public void setRealName(String name);
	
	/**
	 * Using password?
	 */
	public boolean usePassword();
	
	public void setUsePassword(boolean v);
	
	/**
	 * The hashed-password
	 */
	public String getPassword();

	public void setPassword(String password);
	
}
