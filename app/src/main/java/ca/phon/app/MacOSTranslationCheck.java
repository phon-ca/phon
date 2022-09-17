package ca.phon.app;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.*;
import ca.phon.util.OSInfo;
import com.sun.jna.*;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.ptr.IntByReference;

/**
 *
 */
@PhonPlugin(name="startup")
public class MacOSTranslationCheck implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	@Override
	public void startup() throws PluginException {
		if(OSInfo.isMacOs()) {
			if("x86_64".equals(System.getProperty("os.arch"))) {
				Pointer ptr = new Memory(Native.getNativeSize(Integer.class));
				IntByReference ptrSize = new IntByReference();
				final int ret = SystemB.INSTANCE.sysctlbyname("sysctl.proc_translated", ptr, ptrSize, null, 0);
				if (ret == -1) {
					if(Native.getLastError() == 2) { // ENOENT
						LogUtil.info("Running x86_64 untranslated");
					} else {
						LogUtil.info("Error getting sysctl.proc_translated");
					}
				} else {
					if(ret == 1) {
						LogUtil.info("Running x86_64 translated");
					} else {
						LogUtil.info("Running x86_64 untranslated");
					}
				}
			}
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	}
}
