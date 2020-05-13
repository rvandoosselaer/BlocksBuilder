package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.PanelTweens;
import com.simsilica.lemur.anim.SpatialTweens;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.Tweens;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * @author: rvandoosselaer
 */
public class SplashScreenState extends BaseAppState {

    private Panel background;
    private ColorRGBA bgColor = new ColorRGBA().setAsSrgb(0.2f, 0.2f, 0.2f, 1.0f);
    private Panel logo;
    private Node node;
    private Tween fadeOutTween;
    private AnimationState animationState;

    @Override
    protected void initialize(Application app) {
        animationState = GuiGlobals.getInstance().getAnimationState();

        int width = getApplication().getCamera().getWidth();
        int height = getApplication().getCamera().getHeight();

        // bg panel that covers the entire screen
        background = new Panel(width, height, bgColor);
        background.setLocalTranslation(0, height, 99);

        Texture logoImg = GuiGlobals.getInstance().loadTexture("/Textures/logo.png", false, false);
        logo = new Panel(logoImg.getImage().getWidth(), logoImg.getImage().getHeight());
        logo.setBackground(new QuadBackgroundComponent(logoImg));
        logo.setLocalTranslation(width * 0.5f - logo.getPreferredSize().x * 0.5f, height * 0.5f + logo.getPreferredSize().y * 0.5f, 100f);

        // create a tween that fades the panels and detaches them at the end.
        fadeOutTween = Tweens.parallel(
                Tweens.sequence(PanelTweens.fade(background, 1f, 0.0f, 0.5f), SpatialTweens.detach(background)),
                Tweens.sequence(PanelTweens.fade(logo, 1f, 0.0f, 0.5f), SpatialTweens.detach(logo))
        );

        if (node == null) {
            node = ((SimpleApplication) app).getGuiNode();
        }
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        node.attachChild(background);
        node.attachChild(logo);
    }

    @Override
    protected void onDisable() {
        animationState.add(fadeOutTween);
    }

}
