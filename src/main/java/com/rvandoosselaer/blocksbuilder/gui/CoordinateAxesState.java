package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.rvandoosselaer.jmeutils.util.GeometryUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An AppState that renders the coordinate axes in a separate view port. The camera of the view port will stay in sync
 * with the main camera so the coordinate axes will always point to the right direction.
 *
 * @author: rvandoosselaer
 */
public class CoordinateAxesState extends BaseAppState {

    private ViewPort viewPort;
    private Camera camera;
    private Node node = new Node("coordinate axes");
    private Spatial coordinateAxes;
    private Vec2i viewPortSize = new Vec2i(80, 80);
    private Vec2i originalWindowSize; // used to check if the window is resized

    @Override
    protected void initialize(Application app) {
        node.setCullHint(Spatial.CullHint.Never);
        camera = createCamera();
        viewPort = createViewPort();
        coordinateAxes = GeometryUtils.createCoordinateAxes();
        originalWindowSize = Vec2i.fromCamera(app.getCamera());
    }

    @Override
    protected void cleanup(Application app) {
        node.detachAllChildren();
        destroyViewPort(viewPort);
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
    public void update(float tpf) {
        if (isWindowResized()) {
            onWindowResized();
        }

        node.updateLogicalState(tpf);

        Vector3f dir = new Vector3f(getApplication().getCamera().getDirection());
        camera.setLocation(dir.negateLocal().mult(3));
        camera.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    @Override
    public void render(RenderManager rm) {
        node.updateGeometricState();
    }

    private void destroyViewPort(ViewPort viewPort) {
        getApplication().getRenderManager().removeMainView(viewPort);
    }

    private Camera createCamera() {
        Camera camera = new Camera(viewPortSize.x, viewPortSize.y);
        camera.setFrustumPerspective(45, (float) camera.getWidth() / camera.getHeight(), 1f, 5f);
        camera.setLocation(new Vector3f(0, 0, 3));
        camera.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        camera.setViewPort(0, 1, 0, 1);

        return camera;
    }

    private ViewPort createViewPort() {
        ViewPort viewPort = getApplication().getRenderManager().createMainView(node.getName(), camera);
        viewPort.setClearFlags(false, true, false);
        viewPort.attachScene(node);

        return viewPort;
    }

    private boolean isWindowResized() {
        Vec2i currentSize = Vec2i.fromCamera(getApplication().getCamera());
        if (!currentSize.equals(originalWindowSize)) {
            originalWindowSize = currentSize;
            return true;
        }

        return false;
    }

    /**
     * When the window is resized, the camera is set to the size of the window.
     * This is a problem for the coordinateAxesViewPort; we recreate it here.
     */
    private void onWindowResized() {
        destroyViewPort(viewPort);
        camera = createCamera();
        viewPort = createViewPort();
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class Vec2i {

        private final int x;
        private final int y;

        public static Vec2i fromCamera(Camera camera) {
            return new Vec2i(camera.getWidth(), camera.getHeight());
        }

    }

}
