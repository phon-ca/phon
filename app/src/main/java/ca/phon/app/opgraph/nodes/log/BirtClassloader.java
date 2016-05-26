package ca.phon.app.opgraph.nodes.log;

import java.net.URL;
import java.net.URLClassLoader;

public class BirtClassloader extends URLClassLoader {

	public BirtClassloader(URL[] urls) {
		super(urls);
	}

}
