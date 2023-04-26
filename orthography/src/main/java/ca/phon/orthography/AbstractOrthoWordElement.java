package ca.phon.orthography;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

import java.util.Set;

public abstract class AbstractOrthoWordElement implements OrthoWordElement, IExtendable {

    private ExtensionSupport extensionSupport = new ExtensionSupport(getClass(), this);

    public AbstractOrthoWordElement() {
        super();
        extensionSupport.initExtensions();
    }

    @Override
    public Set<Class<?>> getExtensions() {
        return extensionSupport.getExtensions();
    }

    @Override
    public <T> T getExtension(Class<T> cap) {
        return extensionSupport.getExtension(cap);
    }

    @Override
    public <T> T putExtension(Class<T> cap, T impl) {
        return extensionSupport.putExtension(cap, impl);
    }

    @Override
    public <T> T removeExtension(Class<T> cap) {
        return extensionSupport.removeExtension(cap);
    }

    @Override
    public String toString() {
        return getText();
    }

}
