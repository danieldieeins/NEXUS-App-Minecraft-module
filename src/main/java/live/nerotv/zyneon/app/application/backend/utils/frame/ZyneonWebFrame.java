package live.nerotv.zyneon.app.application.backend.utils.frame;

import live.nerotv.Main;
import live.nerotv.openlauncherapi.auth.SimpleMicrosoftAuth;
import live.nerotv.shademebaby.frame.NWebFrame;
import live.nerotv.zyneon.app.application.backend.utils.backend.connector.BackendConnector;
import live.nerotv.zyneon.app.application.backend.utils.backend.connector.BackendConnectorV3;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefDisplayHandlerAdapter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class ZyneonWebFrame extends NWebFrame {

    BackendConnector backendConnector;

    public ZyneonWebFrame(SimpleMicrosoftAuth auth) {
        super("https://danieldieeins.github.io/ZyneonApplicationContent/h/index.html",Main.getDirectoryPath()+"libs/jcef");
        backendConnector = new BackendConnectorV3(auth,this);
        getClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) {
                if(message.contains("[Launcher-Bridge] ")) {
                    String request = message.replace("[Launcher-Bridge] ","");
                    Main.getLogger().debug("[BackendConnector] Received request: \""+request+"\"");
                    backendConnector.resolveRequest(request);
                }
                return super.onConsoleMessage(browser, level, message, source, line);
            }
        });
        try {
            setIconImage(ImageIO.read(getClass().getResource("/icon.png")).getScaledInstance(32,32,Image.SCALE_SMOOTH));
        } catch (IOException ignore) {}
        setSize(1280,820);
    }

    public BackendConnector getBackendConnector() {
        return backendConnector;
    }
}