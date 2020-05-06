package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.rvandoosselaer.blocks.Block;
import com.rvandoosselaer.blocks.BlockRegistry;
import com.rvandoosselaer.blocks.BlocksConfig;
import com.rvandoosselaer.jmeutils.gui.GuiUtils;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.focus.FocusChangeEvent;
import com.simsilica.lemur.focus.FocusChangeListener;
import com.simsilica.lemur.grid.ArrayGridModel;
import com.simsilica.lemur.style.ElementId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * An AppState that renders the blocks window.
 *
 * @author: rvandoosselaer
 */
@Slf4j
public class BlocksState extends BaseAppState {

    @Getter
    @Setter
    private Node node;
    private Container blocks;

    @Override
    protected void initialize(Application app) {
        blocks = layout(createBlocksContainer());

        if (node == null) {
            node = ((SimpleApplication) app).getGuiNode();
        }
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        node.attachChild(blocks);
    }

    @Override
    protected void onDisable() {
        blocks.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        layout(blocks);
    }

    private Container layout(Container container) {
        int margin = 10;
        container.setLocalTranslation(GuiUtils.getWidth() - container.getPreferredSize().getX() - margin, GuiUtils.getHeight() - 10, 99);

        return container;
    }

    private Container createBlocksContainer() {
        Container container = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Even));
        container.addChild(new Label("Blocks", new ElementId("title")));

        TextField filter = container.addChild(new TextField("Filter..."));
        filter.getControl(GuiControl.class).addFocusChangeListener(new FilterTextFieldFocusListener("Filter..."));

        container.addChild(new ExtendedGridPanel(new ArrayGridModel<>(createBlocksGridArray(4))));

        return container;
    }

    private Panel[][] createBlocksGridArray(int cols) {
        BlockRegistry blockRegistry = BlocksConfig.getInstance().getBlockRegistry();
        Collection<Block> blocks = blockRegistry.getAll();
        int rows = (int) Math.ceil((double) blocks.size() / cols);
        Panel[][] grid = new Panel[rows][cols];

        int col = 0;
        int row = 0;
        for (Block block : blocks) {
            Button button = new Button(block.getName());
            button.addClickCommands(btn -> System.out.println(block.getName()));
            grid[row][col++] = button;

            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    @RequiredArgsConstructor
    private static class FilterTextFieldFocusListener implements FocusChangeListener {

        private final String placeholderText;

        @Override
        public void focusGained(FocusChangeEvent event) {
            GuiControl guiControl = (GuiControl) event.getSource();
            TextField textField = (TextField) guiControl.getNode();
            // only clear the text when it's the placeholder text.
            if (textField.getText().equals(placeholderText)) {
                textField.setText("");
            }
        }

        @Override
        public void focusLost(FocusChangeEvent event) {
            GuiControl guiControl = (GuiControl) event.getSource();
            TextField textField = (TextField) guiControl.getNode();
            // set the placeholder text back when the textfield is empty
            if (textField.getText().isEmpty()) {
                textField.setText(placeholderText);
            }
        }

    }

}
