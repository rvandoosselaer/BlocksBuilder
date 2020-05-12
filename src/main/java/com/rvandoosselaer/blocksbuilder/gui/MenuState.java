package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.rvandoosselaer.blocksbuilder.BuilderState;
import com.rvandoosselaer.blocksbuilder.CameraState;
import com.rvandoosselaer.blocksbuilder.PostProcessingState;
import com.rvandoosselaer.jmeutils.gui.GuiUtils;
import com.simsilica.lemur.Action;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.EmptyAction;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.OptionPanel;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.core.VersionedReference;
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
    private CameraPivotPointState cameraPivotPointState;
    private VersionedReference<Boolean> cameraPivotPointRef;
    private Label clickIntervalValue;
    private VersionedReference<Double> clickIntervalRef;
    private PostProcessingState postProcessingState;

    @Override
    protected void initialize(Application app) {
        cameraState = getState(CameraState.class);
        builderState = getState(BuilderState.class);
        optionPanelState = getState(OptionPanelState.class);
        cameraPivotPointState = getState(CameraPivotPointState.class);
        postProcessingState = getState(PostProcessingState.class);
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

        if (cameraPivotPointRef.update()) {
            cameraPivotPointState.setEnabled(cameraPivotPointRef.get());
        }
        if (clickIntervalRef.update()) {
            builderState.setClickRepeatInterval(clickIntervalRef.get().intValue());
            clickIntervalValue.setText(String.format("%d", clickIntervalRef.get().intValue()));
        }
    }

    private void onExit() {
        getApplication().stop();
    }

    private void onExport() {

    }

    private void onSaveAs() {
        builderState.setEnabled(false);
        cameraState.setEnabled(false);

        OptionPanel optionPanel = createOnSaveAsPopup();
        optionPanelState.show(optionPanel);
    }

    private void onSave() {
        boolean isSavedBefore = builderState.getSceneInformation().getFilename() != null;
        if (isSavedBefore) {
            builderState.saveScene(builderState.getSceneInformation().getFilename());
        } else {
            onSaveAs();
        }
    }

    private void onOpen() {
        builderState.setEnabled(false);
        cameraState.setEnabled(false);

        OptionPanel optionPanel = createOnOpenPopup();
        optionPanelState.show(optionPanel);
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

    private OptionPanel createOnSaveAsPopup() {
        OptionPanel optionPanel = new OptionPanel(null, null);
        optionPanel.setTitle("Save scene");
        Container container = optionPanel.getContainer();
        Label label = container.addChild(new Label("Filename:"));
        label.setPreferredSize(label.getPreferredSize().setX(256));
        label.setInsets(new Insets3f(0, 0, 4, 0));
        TextField filenameTextField = container.addChild(new TextField(""));
        filenameTextField.setPreferredSize(filenameTextField.getPreferredSize().setX(256));
        optionPanel.setOptions(new Action("Save") {
            @Override
            public void execute(Button source) {
                String filename = filenameTextField.getText();
                if (filename != null && !filename.isEmpty()) {
                    builderState.saveScene(filenameTextField.getText());
                }
            }
        }, new EmptyAction("Cancel"));

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

        // set focus to the textfield
        GuiGlobals.getInstance().requestFocus(filenameTextField);

        return optionPanel;
    }

    private OptionPanel createOnOpenPopup() {
        OptionPanel optionPanel = new OptionPanel(null, null);
        optionPanel.setTitle("Load scene");
        ListBox<String> scenes = optionPanel.getContainer().addChild(new ListBox<>(new VersionedList<>(builderState.getAllScenes())));
        scenes.setPreferredSize(scenes.getPreferredSize().setX(256));
        optionPanel.setOptions(new Action("Load") {
            @Override
            public void execute(Button source) {
                Integer index = scenes.getSelectionModel().getSelection();
                String scene = index != null ? scenes.getModel().get(index) : null;
                if (scene != null) {
                    builderState.loadScene(scene);
                }
            }
        }, new EmptyAction("Cancel"));

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

        TabbedPanel tabbedPanel = container.addChild(new TabbedPanel());

        // File
        Container fileContainer = tabbedPanel.addTab("File", new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Even)));
        Button newModel = fileContainer.addChild(new Button("New"));
        newModel.addClickCommands(button -> onNew());
        Button openModel = fileContainer.addChild(new Button("Open"));
        openModel.addClickCommands(button -> onOpen());
        Button saveModel = fileContainer.addChild(new Button("Save"));
        saveModel.addClickCommands(button -> onSave());
        Button saveAsModel = fileContainer.addChild(new Button("Save as"));
        saveAsModel.addClickCommands(button -> onSaveAs());
        Button export = fileContainer.addChild(new Button("Export to j3o"));
        export.addClickCommands(button -> onExport());
        Button exit = fileContainer.addChild(new Button("Exit"));
        exit.addClickCommands(button -> onExit());

        // Settings
        Container settingsContainer = tabbedPanel.addTab("Settings", new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.ForcedEven, FillMode.Even)));
        Label cameraPivotPointLabel = new Label("Show camera pivot point:", new ElementId(Label.ELEMENT_ID).child("boolean.label"));
        cameraPivotPointLabel.setTextHAlignment(HAlignment.Right);
        Checkbox cameraPivotPointCheckbox = new Checkbox("", new ElementId(Checkbox.ELEMENT_ID).child("boolean.checkbox"), null);
        getApplication().enqueue(() -> cameraPivotPointCheckbox.setChecked(cameraPivotPointState.isEnabled()));
        cameraPivotPointRef = cameraPivotPointCheckbox.getModel().createReference();
        settingsContainer.addChild(cameraPivotPointLabel);
        settingsContainer.addChild(cameraPivotPointCheckbox, 1);

        Label clickIntervalLabel = new Label("Click repeat speed:", new ElementId(Label.ELEMENT_ID).child("float.label"));
        clickIntervalLabel.setTextHAlignment(HAlignment.Right);
        Slider clickIntervalSlider = new Slider( new DefaultRangedValueModel(0, 1000, builderState.getClickRepeatInterval()), Axis.X, new ElementId(Slider.ELEMENT_ID).child("float.slider"), null);
        clickIntervalSlider.setDelta(50);
        clickIntervalValue = new Label(String.format("%d", builderState.getClickRepeatInterval()), new ElementId(Label.ELEMENT_ID).child("value.label"));
        clickIntervalRef = clickIntervalSlider.getModel().createReference();
        settingsContainer.addChild(clickIntervalLabel);
        settingsContainer.addChild(clickIntervalValue, 1);
        settingsContainer.addChild(clickIntervalSlider, 2);

//        PropertyPanel propertyPanel = new PropertyPanel(null);
//        propertyPanel.addFloatProperty("Sample radius", postProcessingState.getSsaoFilter(), "sampleRadius", 0, 20, 0.1f);
//        propertyPanel.addFloatProperty("Intensity", postProcessingState.getSsaoFilter(), "intensity", 0, 50, 0.1f);
//        propertyPanel.addFloatProperty("Scale", postProcessingState.getSsaoFilter(), "scale", 0, 20, 0.1f);
//        propertyPanel.addFloatProperty("Bias", postProcessingState.getSsaoFilter(), "bias", 0, 20, 0.1f);
//        settingsContainer.addChild(propertyPanel);

        tabbedPanel.setSelectedTab(tabbedPanel.getTabs().get(0));

        return container;
    }

}
