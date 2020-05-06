package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.nx.util.jme3.base.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * @author: rvandoosselaer
 */
@Slf4j
@RequiredArgsConstructor
class ScreenshotState extends BaseAppState implements SceneProcessor {

    private final String path;
    @Setter
    private String filename;
    private ByteBuffer outBuf;
    private int width;
    private int height;
    private Renderer renderer;
    private boolean capture;
    @Setter
    private Consumer<ImageRaster> processFunction;

    @Override
    protected void initialize(Application app) {
        app.getViewPort().addProcessor(this);
        width = app.getCamera().getWidth();
        height = app.getCamera().getHeight();
        outBuf = BufferUtils.createByteBuffer(width * height * (int) Math.ceil(Image.Format.RGBA8.getBitsPerPixel() / 8.0));
        renderer = app.getRenderer();
    }

    @Override
    protected void cleanup(Application app) {
        outBuf.clear();
        BufferUtils.destroyDirectBuffer(outBuf);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        width = w;
        height = h;
        outBuf = BufferUtils.createByteBuffer(width * height * (int) Math.ceil(Image.Format.RGBA8.getBitsPerPixel() / 8.0));
    }

    @Override
    public void preFrame(float tpf) {

    }

    @Override
    public void postQueue(RenderQueue rq) {

    }

    @Override
    public void postFrame(FrameBuffer out) {
        if (!capture) {
            return;
        }

        renderer.readFrameBufferWithFormat(out, outBuf, Image.Format.RGBA8);

        Image image = new Image(Image.Format.RGBA8, width, height, outBuf, ColorSpace.Linear);
        ImageRaster imageRaster = ImageRaster.create(image);

        // send the image to the process function and rewind the buffer
        if (processFunction != null) {
            processFunction.accept(imageRaster);
            outBuf.rewind();
        }

        try {
            ImageUtil.writeImage(image, Paths.get(path, filename + ".png").toFile());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        capture = false;
    }

    @Override
    public void setProfiler(AppProfiler profiler) {

    }

    public void takeScreenshot() {
        capture = true;
    }

}
