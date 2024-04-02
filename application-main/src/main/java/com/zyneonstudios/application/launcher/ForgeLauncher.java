package com.zyneonstudios.application.launcher;

import com.zyneonstudios.Main;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.application.installer.ForgeInstaller;
import com.zyneonstudios.application.installer.java.OperatingSystem;
import com.zyneonstudios.application.integrations.index.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.integrations.index.zyndex.instance.ReadableInstance;
import com.zyneonstudios.application.utils.backend.MinecraftVersion;
import com.zyneonstudios.application.utils.frame.LogFrame;
import fr.flowarg.flowupdater.versions.ForgeVersionType;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import javax.swing.*;
import java.nio.file.Path;

public class ForgeLauncher {

    public void launch(ReadableInstance instance) {
        ZyndexIntegration.update(instance);
        launch(instance.getMinecraftVersion(), instance.getForgeVersion(), ForgeVersionType.valueOf(instance.getForgeType().toUpperCase()), instance.getSettings().getMemory(), Path.of(instance.getPath()));
    }

    public void launch(String minecraftVersion, String forgeVersion, ForgeVersionType forgeType, int ram, Path instancePath) {
        MinecraftVersion.Type type = MinecraftVersion.getType(minecraftVersion);
        if(type!=null) {
            Launcher.setJava(type);
        }
        if(ram<512) {
            ram = 512;
        }
        if(new ForgeInstaller().download(minecraftVersion,forgeVersion,forgeType,instancePath)) {
            NoFramework.ModLoader forge;
            if(forgeType==ForgeVersionType.OLD) {
                forge = NoFramework.ModLoader.OLD_FORGE;
            } else if(forgeType==ForgeVersionType.NEO_FORGE) {
                forge = NoFramework.ModLoader.NEO_FORGE;
            } else {
                forge = NoFramework.ModLoader.FORGE;
            }
            NoFramework framework = new NoFramework(
                    instancePath,
                    Application.auth.getAuthInfos(),
                    GameFolder.FLOW_UPDATER
            );
            if(minecraftVersion.equals("1.7.10")) {
                framework.setCustomModLoaderJsonFileName("1.7.10-Forge"+forgeVersion+".json");
            }
            framework.getAdditionalVmArgs().add("-Xms512M");
            framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
            if(Main.operatingSystem== OperatingSystem.macOS) {
                framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
            }
            try {
                Process game = framework.launch(minecraftVersion, forgeVersion, forge);
                Application.getFrame().executeJavaScript("launchStarted();");
                Application.getFrame().setState(JFrame.ICONIFIED);
                LogFrame log;
                if(Application.logOutput) {
                    log = new LogFrame(game.getInputStream(),"Minecraft "+minecraftVersion+" (with "+forgeType.toString().toLowerCase()+"Forge "+forgeVersion+")");
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
                Main.getLogger().error("[LAUNCHER] Couldn't start Forge "+forgeVersion+" ("+forgeType+") for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
                throw new RuntimeException(e);
            }
        } else {
            Application.getFrame().executeJavaScript("launchDefault();");
            Main.getLogger().error("[LAUNCHER] Couldn't start Forge "+forgeVersion+" ("+forgeType+") for Minecraft "+minecraftVersion+" in "+instancePath+" with "+ram+"M RAM");
        }
    }
}