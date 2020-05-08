package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.rvandoosselaer.blocksbuilder.BuilderState;
import com.rvandoosselaer.blocksbuilder.CameraState;
import com.rvandoosselaer.jmeutils.gui.GuiUtils;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.EmptyAction;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.OptionPanel;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.style.ElementId;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An AppState that renders the main menu.
 *
 * @author: rvandoosselaer
 */
@Slf4j
public class MenuState extends BaseAppState {

    @Getter
    @Setter
    private Node node;
    private Container menu;
    private CameraState cameraState;
    private BuilderState builderState;
    private OptionPanelState optionPanelState;

    @Override
    protected void initialize(Application app) {
        menu = layout(createMenu());
        cameraState = getState(CameraState.class);
        builderState = getState(BuilderState.class);
        optionPanelState = getState(OptionPanelState.class);

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
        builderState.setEnabled(false);
        cameraState.setEnabled(false);

        OptionPanel optionPanel = createOnNewPopup();
        optionPanelState.show(optionPanel);
    }

    private Container layout(Container menu) {
        int margin = 10;
        menu.setLocalTranslation(margin, GuiUtils.getHeight() - margin, 99);

        return menu;
    }

    /**
     * Create the option panel for when the 'New' button is clicked.
     * Any button that is clicked in the panel, will also call the builderState#setEnabled(true) and
     * cameraState#setEnabled(true) methods.
     */
    private OptionPanel createOnNewPopup() {
        OptionPanel optionPanel = new OptionPanel("New scene", "Creating a new scene will remove any unsaved " +
                "changes.\nAre you sure you want to continue?", null,
                new CallMethodAction("Yes", builderState, "clearScene"),
                new EmptyAction("No"));
        ColorRGBA colorRGBA = ((TbtQuadBackgroundComponent) optionPanel.getBackground()).getColor();
        colorRGBA.set(colorRGBA.r, colorRGBA.g, colorRGBA.b, 0.9f);

        for (Node button : optionPanel.getButtons().getLayout().getChildren()) {
            if (button instanceof Button) {
                ((Button) button).setTextHAlignment(HAlignment.Center);
                ((Button) button).addClickCommands(source -> {
                    builderState.setEnabled(true);
                    cameraState.setEnabled(true);
                });
            }
        }

        return optionPanel;
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
