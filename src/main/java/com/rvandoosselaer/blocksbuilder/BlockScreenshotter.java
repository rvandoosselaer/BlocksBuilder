package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.rvandoosselaer.blocks.Block;
import com.rvandoosselaer.blocks.BlocksConfig;
import com.rvandoosselaer.blocks.Chunk;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.mathd.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tool to generate an icon of all the default blocks.
 *
 * @author: rvandoosselaer
 */
public class BlockScreenshotter extends SimpleApplication implements AnalogFunctionListener {

    private Node wrapper;
    private FunctionId yaw = new FunctionId("yaw");
    private FunctionId pitch = new FunctionId("pitch");

    public static void main(String[] args) {
        BlockScreenshotter app = new BlockScreenshotter();
        app.setSettings(createAppSettings());
        app.setShowSettings(false);
        app.start();
    }

    public BlockScreenshotter() {
        super(new DebugKeysAppState(),
                new ScreenshotAppState(System.getProperty("user.dir") + "/assets/Textures/blocks/"),
                new FlyCamAppState());
    }

    @Override
    public void simpleInitApp() {
        BlocksConfig.initialize(assetManager);

        wrapper = new Node();
        wrapper.setLocalRotation(new Quaternion(-0.21907966f, 0.39124483f, 0.08470822f, -0.88879573f));
        rootNode.attachChild(wrapper);

        rootNode.addLight(new AmbientLight(ColorRGBA.White));

        chunk = Chunk.createAt(new Vec3i());

        cam.setLocation(new Vector3f(0.0f, 0.0f, 2.4428942f));

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(new FXAAFilter());
        viewPort.addProcessor(fpp);

        //viewPort.setBackgroundColor(ColorRGBA.White);

//        Material backgroundMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        backgroundMaterial.setTexture("ColorMap", assetManager.loadTexture("/com/simsilica/lemur/icons/bordered-gradient.png"));
//        backgroundMaterial.setColor("Color", new ColorRGBA(0, 0.75f, 0.75f, 0.5f));
//        backgroundMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
//
//        Geometry background = new Geometry("background", new Quad(2, 2));
//        background.setQueueBucket(RenderQueue.Bucket.Sky);
//        background.setMaterial(backgroundMaterial);
//        background.setLocalTranslation(-1f, -1f, 0);
//        background.addControl(new BillboardControl());
//
//        Geometry backgroundClone = background.clone(true);
//        backgroundClone.getMaterial().setColor("Color", ColorRGBA.White);
//        backgroundClone.getMaterial().clearParam("ColorMap");
//        backgroundClone.setQueueBucket(RenderQueue.Bucket.Sky);
//        backgroundClone.setMaterial(backgroundMaterial);
//        backgroundClone.setLocalTranslation(-1f, -1f, -0.1f);
//        backgroundClone.addControl(new BillboardControl());

        //rootNode.attachChild(background);
        //rootNode.attachChild(backgroundClone);

        GuiGlobals.initialize(this);
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.map(yaw, KeyInput.KEY_K);
        inputMapper.map(pitch, KeyInput.KEY_I);
        inputMapper.addAnalogListener(this, yaw, pitch);
    }

    @Override
    public void valueActive(FunctionId func, double value, double tpf) {
        float speed = 1f;
        if (Objects.equals(func, yaw)) {
            wrapper.rotate(new Quaternion().fromAngleAxis((float) (speed * tpf), Vector3f.UNIT_Y));
            System.out.println(wrapper.getLocalRotation());
        } else if (Objects.equals(func, pitch)) {
            wrapper.rotate(new Quaternion().fromAngleAxis((float) (speed * tpf), Vector3f.UNIT_Z));
            System.out.println(wrapper.getLocalRotation());
        }
    }

    private int index = 0;
    private Chunk chunk;

    @Override
    public void simpleUpdate(float tpf) {
        List<Block> blocks = new ArrayList<>(BlocksConfig.getInstance().getBlockRegistry().getAll());
        Block block = blocks.get(index++);
        wrapper.detachAllChildren();

        chunk.addBlock(0, 0, 0, block);
        chunk.update();

        Node node = BlocksConfig.getInstance().getChunkMeshGenerator().createNode(chunk);
        wrapper.attachChild(node);
        node.move(-0.5f, -0.5f, -0.5f);

        ScreenshotAppState screenshotAppState = stateManager.getState(ScreenshotAppState.class);
        screenshotAppState.setFileName(block.getName().replaceAll("\\s", "_"));
        screenshotAppState.setIsNumbered(false);
        screenshotAppState.takeScreenshot();

        if (index >= blocks.size()) {
            stop();
        }
    }

    private static AppSettings createAppSettings() {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(64, 64);
        return settings;
    }

}
