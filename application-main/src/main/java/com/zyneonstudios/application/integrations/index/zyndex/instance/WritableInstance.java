package com.zyneonstudios.application.integrations.index.zyndex.instance;

import com.zyneonstudios.application.Application;
import com.zyneonstudios.nexus.instance.Zynstance;
import java.io.File;

public class WritableInstance extends Zynstance implements Instance {

    private File file;
    private String path;
    private InstanceSettings settings;

    public WritableInstance(File file) {
        super(file);
        init(file);
    }

    public WritableInstance(String path) {
        super(new File(path));
        init(new File(path));
    }

    private void init(File file) {
        this.file = file;
        this.path = Application.getInstancePath()+"instances/"+getId()+"/";
        this.settings = new InstanceSettings(this);
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public InstanceSettings getSettings() {
        return this.settings;
    }

    @Override
    public void setSettings(InstanceSettings settings) {
        this.settings = settings;
    }
}