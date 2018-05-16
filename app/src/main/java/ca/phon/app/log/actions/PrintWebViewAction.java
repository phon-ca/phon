package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import ca.phon.app.hooks.HookableAction;
import javafx.application.Platform;
import javafx.scene.web.WebView;

public class PrintWebViewAction extends HookableAction {

	private WeakReference<WebView> webViewRef;
	
	public PrintWebViewAction(WebView webView) {
		this.webViewRef = new WeakReference<>(webView);
	}
	
	public WebView getWebView() {
		return webViewRef.get();
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		Platform.runLater( () -> { 
			
		});
	}

}
