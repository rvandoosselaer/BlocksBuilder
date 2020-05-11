package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.SimpleApplication;
import com.jme3.material.TechniqueDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Limits;
import com.jme3.system.AppSettings;
import com.rvandoosselaer.blocks.BlocksConfig;
import com.rvandoosselaer.blocks.ChunkManager;
import com.rvandoosselaer.blocks.ChunkManagerState;
import com.rvandoosselaer.blocksbuilder.gui.BlocksState;
import com.rvandoosselaer.blocksbuilder.gui.CameraPivotPointState;
import com.rvandoosselaer.blocksbuilder.gui.CoordinateAxesState;
import com.rvandoosselaer.blocksbuilder.gui.MenuState;
import com.rvandoosselaer.jmeutils.ApplicationGlobals;
import com.rvandoosselaer.jmeutils.ApplicationSettingsFactory;
import com.rvandoosselaer.jmeutils.ViewPortState;
import com.rvandoosselaer.jmeutils.post.FilterPostProcessorState;
import com.rvandoosselaer.jmeutils.util.LogUtils;
import com.simsilica.fx.LightingState;
import com.simsilica.fx.sky.SkyState;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.BaseStyles;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: rvandoosselaer
 */
@Slf4j
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        LogUtils.forwardJULToSlf4j();

        Main main = new Main();
        main.start();
    }

    public Main() {
        super(new FilterPostProcessorState(),
                new LightingState(),
                new PostProcessingState(),
                new OptionPanelState(),
                new ChunkManagerState(new ChunkManager(1)),
                new CameraState(),
                new ViewPortState("camera-pivot-point"),
                new ViewPortState("coordinate-axes"),
                new CameraPivotPointState(),
                new CoordinateAxesState(),
                new SkyState(new ColorRGBA(0.34901962f, 0.5019608f, 0.28235295f, 1.0f), true),
                new BuilderState(),
                new MenuState(),
                new BlocksState()
        );

        setSettings(createSettings());
        setShowSettings(false);
    }

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        ApplicationGlobals.initialize(this);
        BlocksConfig.initialize(assetManager);

        loadGUIStyle();

        removeDefaultKeyMappings();

        setupCamera();

        setupLights();

        loadKeyMappings();

        int anisotropicFilter = renderer.getLimits().getOrDefault(Limits.TextureAnisotropy, 1);
        renderer.setDefaultAnisotropicFilter(anisotropicFilter);
    }

    @Override
    public void requestClose(boolean esc) {
        super.requestClose(esc);
    }

    private static AppSettings createSettings() {
        AppSettings settings = ApplicationSettingsFactory.getAppSettings();
        settings.setTitle(String.format("BlocksBuilder - v%s", VersionHelper.getVersion()));

        return settings;
    }

    private void loadGUIStyle() {
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
    }

    private void removeDefaultKeyMappings() {
        getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
    }

    private void setupCamera() {
        Vector3f chunkSize = BlocksConfig.getInstance().getChunkSize().toVector3f();
        Vector3f cameraPivotPoint = new Vector3f(chunkSize.x * 0.5f, 0, chunkSize.z * 0.5f);
        CameraState cameraState = stateManager.getState(CameraState.class);
        cameraState.setStartingTargetLocation(cameraPivotPoint);
    }

    private void setupLights() {
        LightingState lightingState = stateManager.getState(LightingState.class);
        lightingState.setOrientation(4.215f);
        lightingState.setTimeOfDay(0.203f);
        lightingState.setSunColor(new ColorRGBA(1.5f, 1.5f, 1.5f, 1));
        lightingState.setAmbient(new ColorRGBA(0.3f, 0.3f, 0.3f, 1));

        renderManager.setSinglePassLightBatchSize(3);
        renderManager.setPreferredLightMode(TechniqueDef.LightMode.SinglePassAndImageBased);
    }

    private void loadKeyMappings() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        InputFunctions.initializeDefaultMappings(inputMapper);
    }

}
