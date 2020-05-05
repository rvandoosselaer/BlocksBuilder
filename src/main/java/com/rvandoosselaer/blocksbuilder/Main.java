package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.material.TechniqueDef;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Limits;
import com.jme3.system.AppSettings;
import com.rvandoosselaer.blocksbuilder.gui.MenuState;
import com.rvandoosselaer.jmeutils.ApplicationGlobals;
import com.rvandoosselaer.jmeutils.ApplicationSettingsFactory;
import com.rvandoosselaer.jmeutils.post.FilterPostProcessorState;
import com.rvandoosselaer.jmeutils.util.LogUtils;
import com.simsilica.fx.LightingState;
import com.simsilica.fx.sky.SkyState;
import com.simsilica.lemur.GuiGlobals;
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
        super(new StatsAppState(),
                new FilterPostProcessorState(),
                new LightingState(),
                new PostProcessingState(),
                new MenuState(),
                new BuilderState(),
                new SkyState(new ColorRGBA(0.34901962f, 0.5019608f, 0.28235295f, 1.0f), true),
                new CameraState()

        );

        setSettings(createSettings());
        setShowSettings(false);
    }

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        ApplicationGlobals.initialize(this);

        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);

        removeDefaultMappings();

        int anisotropicFilter = renderer.getLimits().getOrDefault(Limits.TextureAnisotropy, 1);
        renderer.setDefaultAnisotropicFilter(anisotropicFilter);
        renderManager.setSinglePassLightBatchSize(3);
        renderManager.setPreferredLightMode(TechniqueDef.LightMode.SinglePassAndImageBased);

        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        InputFunctions.initializeDefaultMappings(inputMapper);
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

    private void removeDefaultMappings() {
        getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
    }

}
