package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.rvandoosselaer.blocksbuilder.CameraState;
import com.rvandoosselaer.blocksbuilder.ViewPortState;
import com.rvandoosselaer.jmeutils.util.GeometryUtils;

/**
 * An AppState that renders the camera pivot point in an 'overlay' view port.
 *
 * @author: rvandoosselaer
 */
public class CameraPivotPointState extends BaseAppState {

    private Node node;
    private Geometry pivotPoint;
    private Camera pivotPointCamera;
    private CameraState cameraState;

    @Override
    protected void initialize(Application app) {
        pivotPoint = createPivotPoint();
        cameraState = getState(CameraState.class);
        ViewPortState viewPortState = getState(ViewPortState.class);
        node = viewPortState.getNode();
        pivotPointCamera = viewPortState.getCamera();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        node.attachChild(pivotPoint);
    }

    @Override
    protected void onDisable() {
        pivotPoint.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        pivotPoint.setLocalTranslation(cameraState.getTargetLocation());

    }

    @Override
    public void render(RenderManager rm) {
        pivotPointCamera.setLocation(getApplication().getCamera().getLocation());
        pivotPointCamera.setRotation(getApplication().getCamera().getRotation());
    }

    private Geometry createPivotPoint() {
        Geometry geometry = GeometryUtils.createGeometry(new Sphere(8, 8, 0.15f), ColorRGBA.Orange, false);
        geometry.getMaterial().getAdditionalRenderState().setWireframe(true);

        return geometry;
    }

}
