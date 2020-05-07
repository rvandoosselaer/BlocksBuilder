package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Quad;
import com.rvandoosselaer.blocks.Block;
import com.rvandoosselaer.blocks.BlockIds;
import com.rvandoosselaer.blocks.BlockRegistry;
import com.rvandoosselaer.blocks.BlocksConfig;
import com.rvandoosselaer.blocks.ChunkManager;
import com.rvandoosselaer.jmeutils.util.GeometryUtils;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import com.simsilica.mathd.Vec3i;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * An AppState for adding and removing blocks. A grid of 32x32 is rendered around the center (0,0,0) point.
 *
 * @author: rvandoosselaer
 */
@Slf4j
public class BuilderState extends BaseAppState implements StateFunctionListener {

    @Getter
    @Setter
    private Node parentNode;
    private Node builderNode;
    @Getter
    private VersionedHolder<Block> selectedBlock;
    private Geometry grid;
    private Geometry addBlockPlaceholder;
    private InputMapper inputMapper;
    private boolean dragging;

    @Override
    protected void initialize(Application app) {
        selectedBlock = new VersionedHolder<>(getDefaultBlock());
        grid = createGrid(app.getAssetManager());
        addBlockPlaceholder = createAddBlockPlaceholder();
        builderNode = new Node("Builder node");
        builderNode.attachChild(grid);
        inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addStateListener(this, InputFunctions.F_DRAG);

        if (parentNode == null) {
            parentNode = ((SimpleApplication) app).getRootNode();
        }
    }

    @Override
    protected void cleanup(Application app) {
        builderNode.detachAllChildren();
        inputMapper.removeStateListener(this, InputFunctions.F_DRAG);
    }

    @Override
    protected void onEnable() {
        parentNode.attachChild(builderNode);
    }

    @Override
    protected void onDisable() {
        builderNode.removeFromParent();
        addBlockPlaceholder.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        // don't calculate collisions when we are dragging the mouse
        CollisionResult collisionResult = dragging ? null : getCursorCollision();

        if (collisionResult != null) {
            positionAddBlockPlaceholder(collisionResult);
            if (addBlockPlaceholder.getParent() == null) {
                parentNode.attachChild(addBlockPlaceholder);
            }
        } else {
            addBlockPlaceholder.removeFromParent();
        }
    }

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {
        // when we are dragging, hide the placeholders
        if (Objects.equals(func, InputFunctions.F_DRAG)) {
            dragging = InputState.Off != value;
        }
    }

    public void setSelectedBlock(Block block) {
        selectedBlock.setObject(block);
    }

    private Block getDefaultBlock() {
        BlockRegistry blockRegistry = BlocksConfig.getInstance().getBlockRegistry();
        return blockRegistry.get(BlockIds.GRASS);
    }

    private Geometry createGrid(AssetManager assetManager) {
        Geometry grid = new Geometry("grid", new Quad(32, 32));
        grid.setMaterial(assetManager.loadMaterial("Materials/grid.j3m"));
        grid.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        // center the grid and lower it a bit to counter z-fighting with the blocks
        grid.setLocalTranslation(-16, -0.01f, 16);
        grid.setShadowMode(RenderQueue.ShadowMode.Receive);

        return grid;
    }

    private Geometry createAddBlockPlaceholder() {
        Vector3f blockSizeExtents = new Vector3f(1, 1, 1)
                .multLocal(BlocksConfig.getInstance().getBlockScale())
                .multLocal(0.5f);

        return GeometryUtils.createGeometry(new WireBox(blockSizeExtents.x, blockSizeExtents.y, blockSizeExtents.z), ColorRGBA.Yellow, false);
    }

    private CollisionResult getCursorCollision() {
        // compute the direction from the position of the cursor
        Vector2f cursorPosition = new Vector2f(getApplication().getInputManager().getCursorPosition());
        Vector3f cursorPosition3D = getApplication().getCamera().getWorldCoordinates(cursorPosition, 0);
        Vector3f cursorPosition3DTarget = getApplication().getCamera().getWorldCoordinates(cursorPosition, 1);
        Vector3f direction = cursorPosition3DTarget.subtract(cursorPosition3D).normalizeLocal();

        CollisionResults collisionResults = new CollisionResults();
        Ray ray = new Ray(cursorPosition3D, direction);

        builderNode.collideWith(ray, collisionResults);

        return collisionResults.size() > 0 ? collisionResults.getClosestCollision() : null;
    }

    private void positionAddBlockPlaceholder(CollisionResult collisionResult) {
        Vec3i addBlockLocation = ChunkManager.getNeighbourBlockLocation(collisionResult);
        Vector3f blockCenter = ChunkManager.getBlockCenterLocation(addBlockLocation);
        addBlockPlaceholder.setLocalTranslation(blockCenter);
    }

}
