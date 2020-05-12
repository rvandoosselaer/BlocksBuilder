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
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.system.JmeSystem;
import com.rvandoosselaer.blocks.Block;
import com.rvandoosselaer.blocks.BlockIds;
import com.rvandoosselaer.blocks.BlockRegistry;
import com.rvandoosselaer.blocks.BlocksConfig;
import com.rvandoosselaer.blocks.Chunk;
import com.rvandoosselaer.blocks.ChunkManager;
import com.rvandoosselaer.blocks.ChunkManagerListener;
import com.rvandoosselaer.blocks.ChunkManagerState;
import com.rvandoosselaer.blocks.FileRepository;
import com.rvandoosselaer.jmeutils.util.GeometryUtils;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import com.simsilica.mathd.Vec3i;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An AppState for adding and removing blocks. A grid of 32x32 is rendered around the center (16,0,16) point.
 *
 * @author: rvandoosselaer
 */
@Slf4j
public class BuilderState extends BaseAppState {

    @Getter
    @Setter
    private Node parentNode;
    private Node builderNode;
    private Node chunkNode;
    @Getter
    private VersionedHolder<Block> selectedBlock;
    private Geometry grid;
    private Geometry addBlockPlaceholder;
    private Geometry removeBlockPlaceholder;
    private InputMapper inputMapper;
    private boolean dragging;
    private ChunkManager chunkManager;
    private Chunk chunk;
    private ChunkListener chunkListener;
    private InputFunctionListener inputListener;
    @Getter
    private final SceneInformation sceneInformation = new SceneInformation();
    private FileRepository chunkRepository;

    @Override
    protected void initialize(Application app) {
        selectedBlock = new VersionedHolder<>(getDefaultBlock());
        chunk = Chunk.createAt(new Vec3i(0, 0, 0));
        chunkNode = chunk.getNode();
        chunkListener = new ChunkListener();
        chunkManager = getState(ChunkManagerState.class).getChunkManager();
        chunkManager.setChunk(chunk);
        chunkManager.addListener(chunkListener);
        String sceneDir = System.getProperty("scene.dir");
        if (sceneDir == null) {
            String jmeStorageFolder = JmeSystem.getStorageFolder().getAbsolutePath();
            sceneDir = Paths.get(jmeStorageFolder, "BlocksBuilder").toString();
            log.warn("No scene.dir system property found. Using {} as scene's storage directory.", sceneDir);
        }
        chunkRepository = new FileRepository(Paths.get(sceneDir));

        grid = createGrid(app.getAssetManager());
        addBlockPlaceholder = createAddBlockPlaceholder();
        removeBlockPlaceholder = createRemoveBlockPlaceholder();

        builderNode = new Node("Builder node");
        builderNode.attachChild(grid);

        inputListener = new InputFunctionListener();
        inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addStateListener(inputListener, InputFunctions.F_DRAG, InputFunctions.F_PLACE_BLOCK, InputFunctions.F_REMOVE_BLOCK, InputFunctions.F_ROTATE_BLOCK);
        inputMapper.addAnalogListener(inputListener, InputFunctions.F_PLACE_BLOCK, InputFunctions.F_REMOVE_BLOCK);

        if (parentNode == null) {
            parentNode = ((SimpleApplication) app).getRootNode();
        }
        parentNode.attachChild(builderNode);
    }

    @Override
    protected void cleanup(Application app) {
        builderNode.detachAllChildren();
        builderNode.removeFromParent();
        inputMapper.removeStateListener(inputListener, InputFunctions.F_DRAG, InputFunctions.F_PLACE_BLOCK, InputFunctions.F_REMOVE_BLOCK, InputFunctions.F_ROTATE_BLOCK);
        inputMapper.removeAnalogListener(inputListener, InputFunctions.F_PLACE_BLOCK, InputFunctions.F_REMOVE_BLOCK);
        chunkManager.removeListener(chunkListener);
    }

    @Override
    protected void onEnable() {
        inputMapper.activateGroup(InputFunctions.BUILDER_INPUT_GROUP);
    }

    @Override
    protected void onDisable() {
        addBlockPlaceholder.removeFromParent();
        removeBlockPlaceholder.removeFromParent();

        inputMapper.deactivateGroup(InputFunctions.BUILDER_INPUT_GROUP);
    }

    @Override
    public void update(float tpf) {
        // don't calculate collisions when we are dragging the mouse
        CollisionResult collisionResult = dragging ? null : getCursorCollision();

        if (collisionResult != null) {
            positionAddBlockPlaceholder(collisionResult);
            positionRemoveBlockPlaceholder(collisionResult);
        } else {
            addBlockPlaceholder.removeFromParent();
            removeBlockPlaceholder.removeFromParent();
        }
    }

    public void setSelectedBlock(Block block) {
        selectedBlock.setObject(block);
    }

    public void clearScene() {
        // detach and remove the old chunk
        if (chunkNode != null) {
            chunkNode.removeFromParent();
        }
        chunkManager.removeChunk(chunk);
        // create and set new chunk
        chunk = Chunk.createAt(new Vec3i(0, 0, 0));
        chunkNode = chunk.getNode();
        chunkManager.setChunk(chunk);
        // reset the scene info
        sceneInformation.clear();
    }

    public void saveScene(String name) {
        chunkRepository.save(chunk, name);
        sceneInformation.save(name);
        log.info("Saved {} to {}.", name, chunkRepository.getPath());
    }

    public void loadScene(String name) {
        Chunk loadedChunk = chunkRepository.load(name + FileRepository.EXTENSION);
        if (loadedChunk != null) {
            clearScene();
            chunk = loadedChunk;
            chunkNode = loadedChunk.getNode();
            chunkManager.setChunk(loadedChunk);
            chunkManager.requestChunkMeshUpdate(loadedChunk);
            sceneInformation.setFilename(name);
            log.info("Loaded {} from {}.", name, chunkRepository.getPath());
        }
    }

    public List<String> getAllScenes() {
        try {
            return Files.list(chunkRepository.getPath())
                    .filter(path -> path.getFileName().toString().endsWith(FileRepository.EXTENSION))
                    .map(path -> path.getFileName().toString())
                    .map(s -> s.substring(0, s.lastIndexOf(FileRepository.EXTENSION)))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    public int getClickRepeatInterval() {
        return inputListener.getClickInterval();
    }

    public void setClickRepeatInterval(int interval) {
        inputListener.setClickInterval(interval);
    }

    private Block getDefaultBlock() {
        BlockRegistry blockRegistry = BlocksConfig.getInstance().getBlockRegistry();
        return blockRegistry.get(BlockIds.GRASS);
    }

    private Geometry createGrid(AssetManager assetManager) {
        Vector3f chunkSize = BlocksConfig.getInstance().getChunkSize().toVector3f();
        Geometry grid = new Geometry("grid", new Quad(chunkSize.x, chunkSize.z));
        grid.setMaterial(assetManager.loadMaterial("Materials/grid.j3m"));
        grid.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        // position the grid and lower it a bit to counter z-fighting with the blocks
        grid.setLocalTranslation(0, -0.01f, chunkSize.z);
        grid.setShadowMode(RenderQueue.ShadowMode.Receive);

        return grid;
    }

    private Geometry createAddBlockPlaceholder() {
        Vector3f blockSizeExtents = new Vector3f(1, 1, 1)
                .multLocal(BlocksConfig.getInstance().getBlockScale())
                .multLocal(0.495f);

        return GeometryUtils.createGeometry(new WireBox(blockSizeExtents.x, blockSizeExtents.y, blockSizeExtents.z), ColorRGBA.Yellow, false);
    }

    private Geometry createRemoveBlockPlaceholder() {
        Vector3f blockSizeExtents = new Vector3f(1, 1, 1)
                .multLocal(BlocksConfig.getInstance().getBlockScale())
                .multLocal(0.505f);

        Geometry geometry = new Geometry("remove block", new Box(blockSizeExtents.x, blockSizeExtents.y, blockSizeExtents.z));
        geometry.setMaterial(getApplication().getAssetManager().loadMaterial("/Materials/remove-block.j3m"));
        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);

        return geometry;
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
        if (!chunk.containsLocation(addBlockLocation)) {
            addBlockPlaceholder.removeFromParent();
            return;
        }

        Vector3f blockCenter = ChunkManager.getBlockCenterLocation(addBlockLocation);
        addBlockPlaceholder.setLocalTranslation(blockCenter);

        if (addBlockPlaceholder.getParent() == null) {
            parentNode.attachChild(addBlockPlaceholder);
        }
    }

    private void positionRemoveBlockPlaceholder(CollisionResult collisionResult) {
        Vec3i removeBlockLocation = ChunkManager.getBlockLocation(collisionResult);
        if (!chunk.containsLocation(removeBlockLocation)) {
            removeBlockPlaceholder.removeFromParent();
            return;
        }

        Vector3f blockCenter = ChunkManager.getBlockCenterLocation(removeBlockLocation);
        removeBlockPlaceholder.setLocalTranslation(blockCenter);

        boolean isAttached = removeBlockPlaceholder.getParent() != null;
        boolean shouldAttach = chunkManager.getBlock(collisionResult).isPresent();

        if (!isAttached && shouldAttach) {
            parentNode.attachChild(removeBlockPlaceholder);
        } else if (isAttached && !shouldAttach) {
            removeBlockPlaceholder.removeFromParent();
        }

    }

    private void addBlock() {
        chunkManager.addBlock(addBlockPlaceholder.getWorldTranslation(), selectedBlock.getObject());
    }

    private void removeBlock() {
        chunkManager.removeBlock(removeBlockPlaceholder.getWorldTranslation());
    }

    private void rotateBlock() {
        if (removeBlockPlaceholder.getParent() == null) {
            return;
        }

        Optional<Block> selectedBlock = chunkManager.getBlock(removeBlockPlaceholder.getWorldTranslation());
        selectedBlock.ifPresent(block -> chunkManager.addBlock(removeBlockPlaceholder.getWorldTranslation(), getRotatedBlock(block)));
    }

    /**
     * Returns the 90° clockwise rotated block of this block, or the same block when the rotated block is not found.
     */
    private Block getRotatedBlock(Block block) {
        String rotatedBlockName = null;
        if (block.getName().endsWith("left")) {
            rotatedBlockName = block.getName().substring(0, block.getName().length() - 4) + "back";
        } else if (block.getName().endsWith("back")) {
            rotatedBlockName = block.getName().substring(0, block.getName().length() - 4) + "right";
        } else if (block.getName().endsWith("right")) {
            rotatedBlockName = block.getName().substring(0, block.getName().length() - 5) + "front";
        } else if (block.getName().endsWith("front")) {
            rotatedBlockName = block.getName().substring(0, block.getName().length() - 5) + "left";
        }

        if (rotatedBlockName != null) {
            BlockRegistry blockRegistry = BlocksConfig.getInstance().getBlockRegistry();
            Block rotatedBlock = blockRegistry.get(rotatedBlockName);
            if (rotatedBlock != null) {
                return rotatedBlock;
            }
        }

        return block;
    }

    private class ChunkListener implements ChunkManagerListener {

        @Override
        public void onChunkUpdated(Chunk newChunk) {
            // detach the old chunk and attach the new node
            if (chunkNode != null && chunkNode.getParent() != null) {
                chunkNode.removeFromParent();
            }
            builderNode.attachChild(newChunk.getNode());
            chunkNode = newChunk.getNode();
        }

        @Override
        public void onChunkAvailable(Chunk chunk) {
        }

    }

    private class InputFunctionListener implements StateFunctionListener, AnalogFunctionListener {

        private boolean pressed;
        private long lastClickTimestamp;
        // time between consecutive clicks in milliseconds. set to 0 to disable 'repeat' clicking
        @Getter
        @Setter
        private int clickInterval;

        public InputFunctionListener() {
            this(125);
        }

        public InputFunctionListener(int clickInterval) {
            this.clickInterval = clickInterval;
        }

        @Override
        public void valueActive(FunctionId func, double value, double tpf) {
            if (pressed) {
                long currentTimestamp = System.currentTimeMillis();
                // only click x times / second
                boolean shouldClick = lastClickTimestamp + clickInterval <= currentTimestamp;
                if (clickInterval <= 0) {
                    // repeat clicking is disabled, only click when there was not a previous click
                    shouldClick = lastClickTimestamp <= 0;
                }
                if (shouldClick) {
                    if (Objects.equals(func, InputFunctions.F_PLACE_BLOCK)) {
                        addBlock();
                    } else if (Objects.equals(func, InputFunctions.F_REMOVE_BLOCK)) {
                        removeBlock();
                    }
                    lastClickTimestamp = currentTimestamp;
                }
            }
        }

        public void valueChanged(FunctionId func, InputState value, double tpf) {
            // when we are dragging, hide the placeholders
            if (Objects.equals(func, InputFunctions.F_DRAG)) {
                dragging = InputState.Off != value;
            } else if (Objects.equals(func, InputFunctions.F_PLACE_BLOCK) || Objects.equals(func, InputFunctions.F_REMOVE_BLOCK)) {
                // reset the timestamp when we are done clicking
                pressed = InputState.Off != value;
                if (!pressed) {
                    lastClickTimestamp = -1;
                }
            } else if (Objects.equals(func, InputFunctions.F_ROTATE_BLOCK) && value != InputState.Off) {
                rotateBlock();
            }
        }

    }
}
