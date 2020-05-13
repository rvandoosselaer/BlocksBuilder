package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Limits;
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
    private Label anisotropicFilterValue;
    private VersionedReference<Double> anisotropicFilterRef;
    private VersionedReference<Boolean> fxaaRef;
    private VersionedReference<Boolean> ssaoRef;
    private VersionedReference<Boolean> shadowsRef;

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
            builderState.setClickRepeatRate(clickIntervalRef.get().intValue());
            clickIntervalValue.setText(String.format("%d", clickIntervalRef.get().intValue()));
        }
        if (anisotropicFilterRef.update()) {
            getApplication().getRenderer().setDefaultAnisotropicFilter(anisotropicFilterRef.get().intValue());
            anisotropicFilterValue.setText(String.format("%d", anisotropicFilterRef.get().intValue()));
        }
        if (fxaaRef.update()) {
            postProcessingState.getFxaaFilter().setEnabled(fxaaRef.get());
        }
        if (ssaoRef.update()) {
            postProcessingState.getSsaoFilter().setEnabled(ssaoRef.get());
        }
        if (shadowsRef.update()) {
            postProcessingState.getDlsf().setEnabled(shadowsRef.get());
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
        Label cameraPivotPointLabel = createLabel("Camera center:", "boolean.label");
        Checkbox cameraPivotPointCheckbox = new Checkbox("", new ElementId(Checkbox.ELEMENT_ID).child("boolean.checkbox"), null);
        getApplication().enqueue(() -> cameraPivotPointCheckbox.setChecked(cameraPivotPointState.isEnabled()));
        cameraPivotPointRef = cameraPivotPointCheckbox.getModel().createReference();
        settingsContainer.addChild(cameraPivotPointLabel);
        settingsContainer.addChild(cameraPivotPointCheckbox, 1);

        Label clickIntervalLabel = createLabel("Click repeat rate:", "float.label");
        Slider clickIntervalSlider = createSlider(builderState.getClickRepeatRate(), 0, 1000, 50);
        clickIntervalValue = new Label(String.format("%d", builderState.getClickRepeatRate()), new ElementId(Label.ELEMENT_ID).child("value.label"));
        clickIntervalRef = clickIntervalSlider.getModel().createReference();
        settingsContainer.addChild(clickIntervalLabel);
        settingsContainer.addChild(clickIntervalValue, 1);
        settingsContainer.addChild(clickIntervalSlider, 2);

        Label anisotropicFilterLabel = createLabel("Anisotropic filter:", "int.label");
        int maxAnisotropicFilter = getApplication().getRenderer().getLimits().getOrDefault(Limits.TextureAnisotropy, 1);
        int anisotropicFilter = getApplication().getRenderer().getDefaultAnisotropicFilter();
        Slider anisotropicFilterSlider = createSlider(anisotropicFilter, 1, maxAnisotropicFilter, 1);
        anisotropicFilterValue = new Label(String.format("%d", anisotropicFilter), new ElementId(Label.ELEMENT_ID).child("value.label"));
        anisotropicFilterRef = anisotropicFilterSlider.getModel().createReference();
        settingsContainer.addChild(anisotropicFilterLabel);
        settingsContainer.addChild(anisotropicFilterValue, 1);
        settingsContainer.addChild(anisotropicFilterSlider, 2);

        Label fxaaLabel = createLabel("FXAA:", "boolean.label");
        Checkbox fxaaCheckbox = new Checkbox("", new ElementId(Checkbox.ELEMENT_ID).child("boolean.checkbox"), null);
        getApplication().enqueue(() -> fxaaCheckbox.setChecked(postProcessingState.getFxaaFilter().isEnabled()));
        fxaaRef = fxaaCheckbox.getModel().createReference();
        settingsContainer.addChild(fxaaLabel);
        settingsContainer.addChild(fxaaCheckbox, 1);

        Label ssaoLabel = createLabel("SSAO:", "boolean.label");
        Checkbox ssaoCheckbox = new Checkbox("", new ElementId(Checkbox.ELEMENT_ID).child("boolean.checkbox"), null);
        getApplication().enqueue(() -> ssaoCheckbox.setChecked(postProcessingState.getSsaoFilter().isEnabled()));
        ssaoRef = ssaoCheckbox.getModel().createReference();
        settingsContainer.addChild(ssaoLabel);
        settingsContainer.addChild(ssaoCheckbox, 1);

        Label shadowsLabel = createLabel("Shadows:", "boolean.label");
        Checkbox shadowsCheckbox = new Checkbox("", new ElementId(Checkbox.ELEMENT_ID).child("boolean.checkbox"), null);
        getApplication().enqueue(() -> shadowsCheckbox.setChecked(postProcessingState.getDlsf().isEnabled()));
        shadowsRef = shadowsCheckbox.getModel().createReference();
        settingsContainer.addChild(shadowsLabel);
        settingsContainer.addChild(shadowsCheckbox, 1);

//        PropertyPanel propertyPanel = new PropertyPanel(null);
//        propertyPanel.addFloatProperty("Sample radius", postProcessingState.getSsaoFilter(), "sampleRadius", 0, 20, 0.1f);
//        propertyPanel.addFloatProperty("Intensity", postProcessingState.getSsaoFilter(), "intensity", 0, 50, 0.1f);
//        propertyPanel.addFloatProperty("Scale", postProcessingState.getSsaoFilter(), "scale", 0, 20, 0.1f);
//        propertyPanel.addFloatProperty("Bias", postProcessingState.getSsaoFilter(), "bias", 0, 20, 0.1f);
//        settingsContainer.addChild(propertyPanel);

        tabbedPanel.setSelectedTab(tabbedPanel.getTabs().get(0));

        return container;
    }

    private static Label createLabel(String text, String elementId) {
        Label label = new Label(text, new ElementId(Label.ELEMENT_ID).child(elementId));
        label.setTextHAlignment(HAlignment.Right);
        return label;
    }

    private static Slider createSlider(double value, double min, double max, double delta) {
        Slider slider = new Slider(new DefaultRangedValueModel(min, max, value), new ElementId(Slider.ELEMENT_ID).child("float.slider"));
        slider.setDelta(delta);

        return slider;
    }
}
