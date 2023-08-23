package live.nerotv.zyneon.app.backend.launcher;

import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import javafx.application.Platform;
import live.nerotv.Main;
import live.nerotv.zyneon.app.backend.installer.VanillaInstaller;
import live.nerotv.zyneon.app.backend.modpack.Modpack;

import java.nio.file.Path;

public class VanillaLauncher {

    public boolean launch(Modpack modpack, int ram) {
        if(Main.config.get("settings.memory."+modpack.getID())!=null) {
            ram = (int)Main.config.get("settings.memory."+modpack.getID());
        }
        return launch(modpack.getMinecraftVersion(), ram, modpack.getPath());
    }

    public boolean launch(String version, int ram, Path instancePath) {
        if(ram<1024) {
            ram = 1024;
        }
        if(new VanillaInstaller().download(version,instancePath)) {
            NoFramework framework = new NoFramework(
                    instancePath,
                    Main.auth.getAuthInfos(),
                    GameFolder.FLOW_UPDATER
            );
            framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
            try {
                Process p = framework.launch(version, version, NoFramework.ModLoader.VANILLA);
                Main.frame.getBrowser().executeJavaScript("javascript:OpenModal('run')","https://a.nerotv.live/zyneon/application/html/account.html",5);
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
                System.out.println("Error: couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
                return false;
            }
        } else {
            System.out.println("Error: couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
            return false;
        }
    }
}