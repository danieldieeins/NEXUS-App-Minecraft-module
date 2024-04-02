package com.zyneonstudios.application.launcher;

import com.zyneonstudios.Main;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.application.installer.VanillaInstaller;
import com.zyneonstudios.application.installer.java.OperatingSystem;
import com.zyneonstudios.application.integrations.index.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.integrations.index.zyndex.instance.ReadableInstance;
import com.zyneonstudios.application.utils.backend.MinecraftVersion;
import com.zyneonstudios.application.utils.frame.LogFrame;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import javax.swing.*;
import java.nio.file.Path;

public class VanillaLauncher {

    public void launch(ReadableInstance instance) {
        ZyndexIntegration.update(instance);
        launch(instance.getMinecraftVersion(), instance.getSettings().getMemory(), Path.of(instance.getPath()));
    }

    public void launch(String version, int ram, Path instancePath) {
        MinecraftVersion.Type type = MinecraftVersion.getType(version);
        if(type!=null) {
            Launcher.setJava(type);
        }
        if(ram<512) {
            ram = 512;
        }
        if(new VanillaInstaller().download(version,instancePath)) {
            NoFramework framework = new NoFramework(
                    instancePath,
                    Application.auth.getAuthInfos(),
                    GameFolder.FLOW_UPDATER
            );
            framework.getAdditionalVmArgs().add("-Xms512M");
            framework.getAdditionalVmArgs().add("-Xmx4096M");
            if(Main.operatingSystem== OperatingSystem.macOS) {
                framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
            }
            try {
                Process game = framework.launch(version, "", NoFramework.ModLoader.VANILLA);
                Application.getFrame().executeJavaScript("launchStarted();");
                Application.getFrame().setState(JFrame.ICONIFIED);
                LogFrame log;
                if(Application.logOutput) {
                    log = new LogFrame(game.getInputStream(),"Minecraft "+version);
                } else {
                    log = null;
                }
                game.onExit().thenRun(()->{
                    if(log!=null) {
                        log.onStop();
                    }
                    Application.getFrame().setState(JFrame.NORMAL);
                    Application.getFrame().executeJavaScript("launchDefault();");
                });
            } catch (Exception e) {
                Application.getFrame().executeJavaScript("launchDefault();");
                if(!Application.auth.isLoggedIn()) {
                    Application.getFrame().getBrowser().loadURL(Application.getSettingsURL()+"?tab=profile");
                }
                Main.getLogger().error("[LAUNCHER] Couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
                throw new RuntimeException(e);
            }
        } else {
            Application.getFrame().executeJavaScript("launchDefault();");
            Main.getLogger().error("[LAUNCHER] Couldn't start Minecraft Vanilla " + version + " in " + instancePath + " with " + ram + "M RAM");
        }
    }
}