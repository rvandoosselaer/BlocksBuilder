package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import lombok.Getter;

/**
 * An AppState that creates a main view port and handles the lifecycle of this view port. A viewport has a camera which
 * is used to render the scene.
 * A view port has a location on the screen as set by the Camera#setViewPort() method. By default, a view port does not
 * clear the framebuffer, but it can be set to ViewPort#setClearFlags() to clear the framebuffer(s).
 *
 * @author: rvandoosselaer
 */
public class ViewPortState extends BaseAppState {

    @Getter
    private final Node node;
    private final String name;
    @Getter
    private Camera camera;
    @Getter
    private ViewPort viewPort;

    public ViewPortState(String name) {
        this.name = name;
        this.node = new Node(name);
        setId(name);
    }

    @Override
    protected void initialize(Application app) {
        node.setCullHint(Spatial.CullHint.Never);
        camera = app.getCamera().clone();
        viewPort = app.getRenderManager().createMainView(name, camera);
        viewPort.attachScene(node);
    }

    @Override
    protected void cleanup(Application app) {
        node.detachAllChildren();
        app.getRenderManager().removeMainView(viewPort);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        node.updateLogicalState(tpf);
    }

    @Override
    public void render(RenderManager rm) {
        node.updateGeometricState();
    }

}
