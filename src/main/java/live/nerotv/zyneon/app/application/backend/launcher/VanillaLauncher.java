package live.nerotv.zyneon.app.application.backend.launcher;

import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.JavaUtil;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import javafx.application.Platform;
import live.nerotv.Main;
import live.nerotv.openlauncherapi.auth.SimpleMicrosoftAuth;
import live.nerotv.zyneon.app.application.backend.installer.VanillaInstaller;
import live.nerotv.zyneon.app.application.backend.instance.Instance;
import live.nerotv.zyneon.app.application.backend.utils.frame.ZyneonWebFrame;

import java.io.File;
import java.nio.file.Path;

public class VanillaLauncher {

    private ZyneonWebFrame frame;
    private SimpleMicrosoftAuth auth;

    public VanillaLauncher(SimpleMicrosoftAuth auth, ZyneonWebFrame frame) {
        this.auth = auth;
        this.frame = frame;
    }

    public boolean launch(Instance instance, int ram) {
        String id = instance.getID();
        String ramID = id.replace(".","").replace("/","");
        if(Main.config.get("settings.memory."+ramID)!=null) {
            ram = Main.config.getInteger("settings.memory."+ramID);
        }
        if(!new File(instance.getPath()+"/pack.zip").exists()) {
            frame.getBrowser().executeJavaScript("javascript:OpenModal('install')","https://a.nerotv.live/zyneon/application/html/account.html",5);
            instance.update();
        }
        if(!instance.checkVersion()) {
            frame.getBrowser().executeJavaScript("javascript:OpenModal('install')","https://a.nerotv.live/zyneon/application/html/account.html",5);
            instance.update();
        }
        return launch(instance.getMinecraftVersion(), ram, Path.of(instance.getPath()));
    }

    public boolean launch(String version, int ram, Path instancePath) {
        frame.getBrowser().executeJavaScript("javascript:OpenModal('run')","https://a.nerotv.live/zyneon/application/html/account.html",5);
        if(MinecraftVersion.getType(version)!=null) {
            MinecraftVersion.Type type = MinecraftVersion.getType(version);
            if(type.equals(MinecraftVersion.Type.LEGACY)) {
                JavaUtil.setJavaCommand(null);
                System.setProperty("java.home", Main.getDirectoryPath()+"libs/jre-8");
            } else if(type.equals(MinecraftVersion.Type.SEMI_NEW)) {
                JavaUtil.setJavaCommand(null);
                System.setProperty("java.home", Main.getDirectoryPath()+"libs/jre-11");
            } else if(type.equals(MinecraftVersion.Type.NEW)) {
                JavaUtil.setJavaCommand(null);
                System.setProperty("java.home", Main.getDirectoryPath()+"libs/jre");
            }
        }
        if(ram<1024) {
            ram = 1024;
        }
        if(new VanillaInstaller().download(version,instancePath)) {
            NoFramework framework = new NoFramework(
                    instancePath,
                    auth.getAuthInfos(),
                    GameFolder.FLOW_UPDATER
            );
            framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
            try {
                Process p = framework.launch(version, version, NoFramework.ModLoader.VANILLA);
                Platform.runLater(() -> {
                    try {
                        p.waitFor();
                        Platform.exit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                return true;
            } catch (Exception e) {
                Main.getLogger().error("Error: couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
                return false;
            }
        } else {
            Main.getLogger().error("Error: couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
            return false;
        }
    }
}