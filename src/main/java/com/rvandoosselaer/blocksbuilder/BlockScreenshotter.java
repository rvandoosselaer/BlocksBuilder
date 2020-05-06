package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.texture.image.ImageRaster;
import com.rvandoosselaer.blocks.Block;
import com.rvandoosselaer.blocks.BlocksConfig;
import com.rvandoosselaer.blocks.Chunk;
import com.simsilica.mathd.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tool to generate an icon of all the default blocks.
 *
 * @author: rvandoosselaer
 */
public class BlockScreenshotter extends SimpleApplication {

    private Node wrapper;
    private int index = 0;
    private Chunk chunk;
    private ScreenshotState screenshotState;

    public static void main(String[] args) {
        BlockScreenshotter app = new BlockScreenshotter();
        app.setSettings(createAppSettings());
        app.setShowSettings(false);
        app.start();
    }

    public BlockScreenshotter() {
        super(new DebugKeysAppState(),
                new ScreenshotState(System.getProperty("user.dir") + "/assets/Textures/blocks/"),
                new FlyCamAppState());
    }

    @Override
    public void simpleInitApp() {
        BlocksConfig.initialize(assetManager);

        // rotate the block, to have a better angle when taking the screenshot
        wrapper = new Node();
        Quaternion yaw = new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Y);
        Quaternion pitch = new Quaternion().fromAngleAxis(30 * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
        wrapper.setLocalRotation(pitch.mult(yaw));
        wrapper.setLocalTranslation(0, 0.1f, 0);

        rootNode.attachChild(wrapper);
        rootNode.addLight(new AmbientLight(ColorRGBA.White));

        chunk = Chunk.createAt(new Vec3i());

        screenshotState = stateManager.getState(ScreenshotState.class);
        screenshotState.setProcessFunction(new TransparentPixelProcessor());

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(new FXAAFilter());
        viewPort.addProcessor(fpp);

        cam.setLocation(new Vector3f(0.0f, 0.0f, 2.4428942f));
    }

    @Override
    public void simpleUpdate(float tpf) {
        List<Block> blocks = new ArrayList<>(BlocksConfig.getInstance().getBlockRegistry().getAll());

        Block block = blocks.get(index++);
        String blockName = block.getName().replaceAll("\\s", "_");

        wrapper.detachAllChildren();

        chunk.addBlock(0, 0, 0, block);
        chunk.update();

        Node node = BlocksConfig.getInstance().getChunkMeshGenerator().createNode(chunk);
        wrapper.attachChild(node);
        node.move(-0.5f, -0.5f, -0.5f);

        screenshotState.setFilename(blockName);
        screenshotState.takeScreenshot();

        if (index >= blocks.size()) {
            stop();
        }
    }

    private static AppSettings createAppSettings() {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(64, 64);
        return settings;
    }

    /**
     * A processor function that changes the black pixels with transparent pixels.
     */
    private static class TransparentPixelProcessor implements Consumer<ImageRaster> {

        @Override
        public void accept(ImageRaster imageRaster) {
            for (int w = 0; w < imageRaster.getWidth(); w++) {
                for (int h = 0; h < imageRaster.getHeight(); h++) {
                    ColorRGBA pixel = imageRaster.getPixel(w, h);
                    if (pixel.equals(ColorRGBA.Black)) {
                        imageRaster.setPixel(w, h, ColorRGBA.BlackNoAlpha);
                    }
                }
            }
        }

    }

}
