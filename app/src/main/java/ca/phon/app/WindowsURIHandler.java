package ca.phon.app;

import ca.phon.app.actions.PhonURISchemeHandler;
import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonWorker;
import com.sun.jna.platform.win32.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Create a hidden message window for handling uri load requests on windows.
 * The window will respond to WM_USER messages with the lparam variable holding
 * a guid long value. This guid will be used to locate a file named with the
 * guid value in hex format in the user's application data folder. Once processed
 * the file will be deleted.
 */
public final class WindowsURIHandler implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

    public final static String WINDOW_CLASS = "Phon_uri_handler";

    private final static String URI_HANDLER_FOLDER = "uri_requests";

    @Override
    public void startup() throws PluginException {
        if(!OSInfo.isWindows()) return;

        final File uriHandlerFolder = new File(PrefHelper.getUserDataFolder(), URI_HANDLER_FOLDER);
        if(!uriHandlerFolder.exists()) {
            uriHandlerFolder.mkdirs();
        }

        createWindow(WINDOW_CLASS);
    }

    public static File uriRequestFileFromId(long messageId) {
        final File uriHandlerFolder = new File(PrefHelper.getUserDataFolder(), URI_HANDLER_FOLDER);
        final File uriRequestFile = new File(uriHandlerFolder, Long.toHexString(messageId) + ".txt");
        return uriRequestFile;
    }

    private void processMessageWithId(long messageId) throws IOException, URISyntaxException, PluginException {
        final File uriRequestFile = uriRequestFileFromId(messageId);

        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(new FileInputStream(uriRequestFile), "UTF-8"));
        final String line = reader.readLine();
        reader.close();

        final String uriRequest = line.trim();
        final PhonURISchemeHandler uriSchemeHandler = new PhonURISchemeHandler();
        uriSchemeHandler.openURI(new URI(uriRequest));

        uriRequestFile.delete();
    }

    public void createWindow(final String windowClass) {
        // Runs it in a specific thread because the main thread is blocked in infinite loop otherwise.
        PhonWorker.invokeOnNewWorker(() -> {
            try {
                createWindowAndLoop(windowClass);
            } catch (Throwable t) {
                LogUtil.warning(t);
            }
        });
    }

    private void createWindowAndLoop(String windowClass) {
        // define new window class
        WinDef.HMODULE hInst = Kernel32.INSTANCE.GetModuleHandle("");

        WinUser.WNDCLASSEX wClass = new WinUser.WNDCLASSEX();
        wClass.hInstance = hInst;
        wClass.lpfnWndProc = new WinUser.WindowProc() {

            @Override
            public WinDef.LRESULT callback(WinDef.HWND hwnd, int uMsg, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
                // log(hwnd + " - received a message : " + uMsg);
                switch (uMsg) {
                    case WinUser.WM_CREATE: {
                        LogUtil.info("Windows message handler window created");
                        return new WinDef.LRESULT(0);
                    }
                    case WinUser.WM_CLOSE:
                        LogUtil.info("Windows message handler window destroyed");
                        User32.INSTANCE.DestroyWindow(hwnd);
                        return new WinDef.LRESULT(0);
                    case WinUser.WM_DESTROY: {
                        User32.INSTANCE.PostQuitMessage(0);
                        return new WinDef.LRESULT(0);
                    }
                    case WinUser.WM_USER: {
                        LogUtil.info("WM_USER message with lParam " + Long.toHexString(lParam.longValue()));
                        if(lParam.longValue() != 0) {
                            PhonWorker.getInstance().invokeLater(() -> {
                                try {
                                    processMessageWithId(lParam.longValue());
                                } catch (IOException | URISyntaxException | PluginException e) {
                                    LogUtil.warning(e);
                                }
                            });
                        }
                        return new WinDef.LRESULT(0);
                    }

                    default:
                        return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
                }
            }
        };
        wClass.lpszClassName = windowClass;

        // register window class
        User32.INSTANCE.RegisterClassEx(wClass);
        //getLastError();

        // create new window
        WinDef.HWND hWnd = User32.INSTANCE.CreateWindowEx(User32.WS_EX_TOPMOST, windowClass,
                "Hidden helper window, used only to catch uri request events", 0, 0, 0, 0, 0, null, null, hInst,
                null);

        //getLastError();

        WinUser.MSG msg = new WinUser.MSG();
        while (User32.INSTANCE.GetMessage(msg, hWnd, 0, 0) > 0) {
            User32.INSTANCE.TranslateMessage(msg);
            User32.INSTANCE.DispatchMessage(msg);
        }

        User32.INSTANCE.UnregisterClass(windowClass, hInst);
        User32.INSTANCE.DestroyWindow(hWnd);
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
