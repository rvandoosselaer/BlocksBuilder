package com.rvandoosselaer.blocksbuilder;

import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A scene processor that recalculates the camera perspective when the viewport is resized.
 *
 * @author: rvandoosselaer
 */
@NoArgsConstructor
public class ResizeProcessor implements SceneProcessor {

    private boolean initialized;
    @Getter
    @Setter
    private float fieldOfView = 45f;
    @Getter
    @Setter
    private float nearFrustum = 1f;
    @Getter
    @Setter
    private float farFrustum = 1000f;

    public ResizeProcessor(float fieldOfView, float nearFrustum, float farFrustum) {
        this.fieldOfView = fieldOfView;
        this.nearFrustum = nearFrustum;
        this.farFrustum = farFrustum;
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        this.initialized = true;
        reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        float aspect = (float) w / h;
        Camera camera = vp.getCamera();
        camera.setFrustumPerspective(fieldOfView, aspect, nearFrustum, farFrustum);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue rq) {
    }

    @Override
    public void postFrame(FrameBuffer out) {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void setProfiler(AppProfiler profiler) {
    }

}
