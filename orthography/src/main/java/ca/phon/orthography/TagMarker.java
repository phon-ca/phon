package ca.phon.orthography;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

import java.util.Optional;
import java.util.Set;

public class TagMarker implements OrthoElement {

    private final ExtensionSupport extensionSupport = new ExtensionSupport(TagMarker.class, this);

    private final TagMarkerType type;

    public TagMarker(TagMarkerType type) {
        this.type = type;
    }

    public TagMarkerType getType() {
        return type;
    }

    @Override
    public String text() {
        return type.getChar() + "";
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

}
