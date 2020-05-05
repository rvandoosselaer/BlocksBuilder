package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import lombok.Getter;
import lombok.Setter;

/**
 * An AppState for adding and removing blocks. A grid of 32x32 is rendered around the center (0,0,0) point.
 *
 * @author: rvandoosselaer
 */
public class BuilderState extends BaseAppState {

    @Getter
    @Setter
    private Node node;
    private Geometry grid;

    @Override
    protected void initialize(Application app) {
        grid = createGrid(app.getAssetManager());

        if (node == null) {
            node = ((SimpleApplication) app).getRootNode();
        }
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        node.attachChild(grid);
    }

    @Override
    protected void onDisable() {
        grid.removeFromParent();
    }

    private Geometry createGrid(AssetManager assetManager) {
        Geometry grid = new Geometry("grid", new Quad(32, 32));
        grid.setMaterial(assetManager.loadMaterial("Materials/grid.j3m"));
        grid.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        grid.setLocalTranslation(-16, 0, 16);
        grid.setShadowMode(RenderQueue.ShadowMode.Receive);

        return grid;
    }

}
