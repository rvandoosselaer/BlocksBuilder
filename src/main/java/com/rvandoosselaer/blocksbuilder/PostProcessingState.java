package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;
import com.rvandoosselaer.jmeutils.post.FilterPostProcessorState;
import com.simsilica.fx.LightingState;

/**
 * An AppState that handles all post processing effects.
 *
 * @author: rvandoosselaer
 */
public class PostProcessingState extends BaseAppState {

    private FilterPostProcessorState fpps;
    private DirectionalLight directionalLight;
    private FXAAFilter fxaaFilter;
    private SSAOFilter ssaoFilter;
    private DirectionalLightShadowFilter dlsf;

    @Override
    protected void initialize(Application app) {
        fpps = getState(FilterPostProcessorState.class);
        LightingState lightingState = getState(LightingState.class);
        lightingState.setOrientation(4.215f);
        lightingState.setTimeOfDay(0.203f);
        lightingState.setSunColor(new ColorRGBA(1.5f, 1.5f, 1.5f, 1));
        lightingState.setAmbient(new ColorRGBA(0.3f, 0.3f, 0.3f, 1));
        directionalLight = lightingState.getSun();


        dlsf = createDirectionalLightFilter();
        ssaoFilter = createSSAOFilter();
        fxaaFilter = createFXAAFilter();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        fpps.addFilter(dlsf);
        fpps.addFilter(ssaoFilter);
        fpps.addFilter(fxaaFilter);
    }

    @Override
    protected void onDisable() {
        fpps.removeFilter(fxaaFilter);
        fpps.removeFilter(ssaoFilter);
        fpps.removeFilter(dlsf);
    }

    private FXAAFilter createFXAAFilter() {
        return new FXAAFilter();
    }

    private SSAOFilter createSSAOFilter() {
        return new SSAOFilter();
    }

    private DirectionalLightShadowFilter createDirectionalLightFilter() {
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(getApplication().getAssetManager(), 4096, 4);
        dlsf.setLight(directionalLight);
        dlsf.setLambda(1);
        dlsf.setEdgesThickness(4);
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);

        return dlsf;
    }

}
