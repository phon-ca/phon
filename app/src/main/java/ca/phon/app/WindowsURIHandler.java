/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.sun.jna.*;
import com.sun.jna.platform.win32.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Create a hidden message window for handling uri load requests on windows.
 * The window will respond to WM_USER messages with the lparam variable holding
 * a guid long value. This guid will be used to locate a file named with the
 * guid value in hex format in the user's application data folder. Once processed
 * the file will be deleted.
 */
public final class WindowsURIHandler implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

    private final static String WINDOW_CLASS = "Phon_uri_handler";

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

        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(uriRequestFile), "UTF-8"))) {
            final String line = reader.readLine();

            final String uriRequest = line.trim();
            final PhonURISchemeHandler uriSchemeHandler = new PhonURISchemeHandler();
            uriSchemeHandler.openURI(new URI(uriRequest));
        } finally {
            uriRequestFile.delete();
        }
    }

    private void createWindow(final String windowClass) {
        // Runs it in a specific thread because the main thread is blocked in infinite loop otherwise.
        PhonWorker.invokeOnNewWorker(() -> {
            try {
                createWindowAndLoop(windowClass);
            } catch (Throwable t) {
                LogUtil.warning(t);
            }
        });
    }

    public static WinDef.HWND getMessageHWND() {
        return determineHWNDFromWindowClass(WINDOW_CLASS);
    }

    private static void sendOpenURIMessage(URI uri) throws IOException {
        WinDef.HWND hWnd = WindowsURIHandler.getMessageHWND();
        if(hWnd == null) {
           openPhonWithURI(uri);
        } else {
            final UUID uuid = UUID.randomUUID();
            final long messageId = uuid.getLeastSignificantBits();

            final File uriRequestFile = WindowsURIHandler.uriRequestFileFromId(messageId);
            try(final PrintWriter printWriter =
                        new PrintWriter(new OutputStreamWriter(new FileOutputStream(uriRequestFile), "UTF-8"))) {
                printWriter.write(uri.toString());
                printWriter.write("\r\n");
                printWriter.flush();
            }

            WinDef.LRESULT result = User32.INSTANCE.SendMessage(hWnd, WinUser.WM_USER, new WinDef.WPARAM(0),
                    new WinDef.LPARAM(messageId));
            if(result.intValue() != 0) {
                throw new IOException("Unable to send WM_USER message");
            }
        }
    }

    private void createWindowAndLoop(String windowClass) {
        // define new window class
        WinDef.HMODULE hInst = Kernel32.INSTANCE.GetModuleHandle("");

        WinUser.WNDCLASSEX wClass = new WinUser.WNDCLASSEX();
        wClass.hInstance = hInst;
        wClass.lpfnWndProc = (WinUser.WindowProc) (hwnd, uMsg, wParam, lParam) -> {
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
        };
        wClass.lpszClassName = windowClass;

        // register window class
        User32.INSTANCE.RegisterClassEx(wClass);
        //getLastError();

        // create new window
        WinDef.HWND hWnd = User32.INSTANCE.CreateWindowEx(User32.WS_EX_TOPMOST, windowClass,
                "Phon URI message handler", 0, 0, 0, 0, 0, null, null, hInst,
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

    private static WinDef.HWND determineHWNDFromWindowClass(String windowClass) {
        WindowsURIHandler.CallBackFindWindowHandleByWindowclass cb = new WindowsURIHandler.CallBackFindWindowHandleByWindowclass(windowClass);
        User32.INSTANCE.EnumWindows(cb, null);
        return cb.getFoundHwnd();
    }

    private static class CallBackFindWindowHandleByWindowclass implements WinUser.WNDENUMPROC {

        private WinDef.HWND found;

        private String windowClass;

        public CallBackFindWindowHandleByWindowclass(String windowClass) {
            this.windowClass = windowClass;
        }

        @Override
        public boolean callback(WinDef.HWND hWnd, Pointer data) {

            char[] windowText = new char[512];
            User32.INSTANCE.GetClassName(hWnd, windowText, windowText.length);
            String className = Native.toString(windowText);

            if (windowClass.equalsIgnoreCase(className)) {
                // Found handle. No determine root window...
                WinDef.HWND hWndAncestor = User32.INSTANCE.GetAncestor(hWnd, User32.GA_ROOTOWNER);
                found = hWndAncestor;
                return false;
            }
            return true;
        }

        public WinDef.HWND getFoundHwnd() {
            return this.found;
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

    private final static String USE_MSG = "Usage phon_uri_handler <phon:uri>";

    /**
     * Send message to custom message handler window using WM_USER message. A new launcher will
     * be created by the installer at bin/phon_uri_handler.exe which executes this program. If
     * Phon is not running (i.e., the message window is not found) it is opened with the uri
     * as an argument.
     */
    public static void main(String[] args) {
        if(args.length != 1) {
            System.err.println(USE_MSG);
            System.exit(1);
        }

        final String uri = args[0].trim();
        try {
            URI parsedURI = new URI(uri);
            sendOpenURIMessage(parsedURI);
            System.exit(0);
        } catch (IOException | URISyntaxException e) {
            System.err.println("Invalid URI " + uri + ", " + e.getMessage());
            System.exit(2);
        }
    }

    private static void openPhonWithURI(URI uri) throws IOException {
        String exeName = "Phon";
        if(VersionInfo.getInstance().getPreRelease() != null) {
            exeName += "-" + VersionInfo.getInstance().getPreRelease().split("\\.")[0];
        }
        exeName += ".exe";

        final ProcessBuilder pb = new ProcessBuilder(exeName, uri.toString());
        pb.start();
    }

}
