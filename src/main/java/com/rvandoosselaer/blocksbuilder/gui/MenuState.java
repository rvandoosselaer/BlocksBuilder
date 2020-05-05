package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.rvandoosselaer.jmeutils.gui.GuiUtils;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.style.ElementId;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: rvandoosselaer
 */
@Slf4j
public class MenuState extends BaseAppState {

    @Getter
    @Setter
    private Node node;
    private Container menu;

    @Override
    protected void initialize(Application app) {
        menu = layout(createMenu());


        if (node == null) {
            node = ((SimpleApplication) app).getGuiNode();
        }
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        node.attachChild(menu);
    }

    @Override
    protected void onDisable() {
        menu.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        layout(menu);
    }

    private void onExit() {
        getApplication().stop();
    }

    private void onExport() {

    }

    private void onSaveAs() {

    }

    private void onSave() {

    }

    private void onOpen() {

    }

    private void onNew() {

    }

    private Container layout(Container menu) {
        int margin = 10;
        menu.setLocalTranslation(margin, GuiUtils.getHeight() - margin, 99);

        return menu;
    }

    private Container createMenu() {
        Container container = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Even));
        ColorRGBA colorRGBA = ((TbtQuadBackgroundComponent) container.getBackground()).getColor();
        colorRGBA.set(colorRGBA.r, colorRGBA.g, colorRGBA.b, 0.9f);

        container.addChild(new Label("BlocksBuilder", new ElementId("title")));

        Button newModel = container.addChild(new Button("New"));
        newModel.addClickCommands(button -> onNew());

        Button openModel = container.addChild(new Button("Open"));
        openModel.addClickCommands(button -> onOpen());

        Button saveModel = container.addChild(new Button("Save"));
        saveModel.addClickCommands(button -> onSave());

        Button saveAsModel = container.addChild(new Button("Save as"));
        saveAsModel.addClickCommands(button -> onSaveAs());

        Button export = container.addChild(new Button("Export to j3o"));
        export.addClickCommands(button -> onExport());

        Button exit = container.addChild(new Button("Exit"));
        exit.addClickCommands(button -> onExit());

        return container;
    }

}
