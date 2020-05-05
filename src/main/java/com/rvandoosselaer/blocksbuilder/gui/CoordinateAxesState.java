package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.rvandoosselaer.blocksbuilder.ViewPortState;
import com.rvandoosselaer.jmeutils.util.GeometryUtils;

/**
 * An AppState that renders the coordinate axes in a separate view port. The camera of the view port will stay in sync
 * with the main camera so the coordinate axes will always point to the right direction.
 *
 * @author: rvandoosselaer
 */
public class CoordinateAxesState extends BaseAppState {

    private Node node;
    private Node coordinateAxes;
    private Camera coordinateAxesCamera;
    private Vector2f cameraSize = new Vector2f(80, 80);

    @Override
    protected void initialize(Application app) {
        coordinateAxes = GeometryUtils.createCoordinateAxes();

        ViewPortState viewPortState = getStateManager().getState("coordinate-axes", ViewPortState.class);
        node = viewPortState.getNode();
        coordinateAxesCamera = viewPortState.getCamera();
        float right = cameraSize.x / app.getCamera().getWidth();
        float top = cameraSize.y / app.getCamera().getHeight();
        coordinateAxesCamera.setViewPort(0, right, 0, top);
        float aspect = cameraSize.x / cameraSize.y;
        coordinateAxesCamera.setFrustumPerspective(45, aspect, 1f, 5f);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        node.attachChild(coordinateAxes);
    }

    @Override
    protected void onDisable() {
        coordinateAxes.removeFromParent();
    }

    @Override
    public void render(RenderManager rm) {
        Vector3f dir = new Vector3f(getApplication().getCamera().getDirection());
        coordinateAxesCamera.setLocation(dir.negateLocal().mult(3));
        coordinateAxesCamera.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

}
