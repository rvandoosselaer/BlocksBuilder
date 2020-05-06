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
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.focus.FocusChangeEvent;
import com.simsilica.lemur.focus.FocusChangeListener;
import com.simsilica.lemur.grid.ArrayGridModel;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
    private Container blocksContainer;
    private ExtendedGridPanel blocksGrid;
    private String filterPlaceholderText = "Filter...";
    private VersionedReference<DocumentModel> filterRef;
    private Label selectedBlock;

    @Override
    protected void initialize(Application app) {
        blocksContainer = layout(createBlocksContainer());

        if (node == null) {
            node = ((SimpleApplication) app).getGuiNode();
        }
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        node.attachChild(blocksContainer);
    }

    @Override
    protected void onDisable() {
        blocksContainer.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        layout(blocksContainer);

        if (filterRef.update()) {
            filterBlocks(filterRef.get().getText());
        }
    }

    private Container layout(Container container) {
        int margin = 10;
        container.setLocalTranslation(GuiUtils.getWidth() - container.getPreferredSize().getX() - margin, GuiUtils.getHeight() - 10, 99);

        return container;
    }

    private void filterBlocks(String text) {
        if (text == null || text.isEmpty() || text.equals(filterPlaceholderText)) {
            return;
        }

        String[] split = text.split("\\s+");

        // filter out the blocks that don't match per word.
        List<Block> filteredBlocks = new ArrayList<>(getBlocks());
        for (String s : split) {
            filteredBlocks.removeAll(getBlocks().stream()
                    .filter(block -> !block.getName().contains(s))
                    .collect(Collectors.toList()));
        }

        blocksGrid.setModel(new ArrayGridModel<>(createBlocksGridArray(filteredBlocks, 4)));
    }

    private Container createBlocksContainer() {
        Container container = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Even));
        container.addChild(new Label("Blocks", new ElementId("title")));

        Container filterWrapper = container.addChild(new Container(new BorderLayout(), new ElementId("wrapper")));
        TextField filter = filterWrapper.addChild(new TextField("Filter..."), BorderLayout.Position.Center);
        filter.getControl(GuiControl.class).addFocusChangeListener(new FilterTextFieldFocusListener(filterPlaceholderText));
        filterRef = filter.getDocumentModel().createReference();
        Button clearFilter = filterWrapper.addChild(new Button("Clear"), BorderLayout.Position.East);
        clearFilter.addClickCommands(source -> clearFilter(filter));

        blocksGrid = container.addChild(new ExtendedGridPanel(new ArrayGridModel<>(createBlocksGridArray(getBlocks(), 4))));

        Container selectedBlockWrapper = container.addChild(new Container(new BorderLayout(), new ElementId("wrapper")));
        selectedBlockWrapper.addChild(new Label("Block: "), BorderLayout.Position.West);
        selectedBlock = selectedBlockWrapper.addChild(new Label("", new ElementId(Label.ELEMENT_ID).child("value.label")), BorderLayout.Position.Center);
        selectedBlock.setTextHAlignment(HAlignment.Left);

        return container;
    }

    private void clearFilter(TextField filter) {
        filter.setText(filterPlaceholderText);
        blocksGrid.setModel(new ArrayGridModel<>(createBlocksGridArray(getBlocks(), 4)));
    }

    private Collection<Block> getBlocks() {
        BlockRegistry blockRegistry = BlocksConfig.getInstance().getBlockRegistry();
        return blockRegistry.getAll();
    }

    private Panel[][] createBlocksGridArray(Collection<Block> blocks, int cols) {
        int rows = (int) Math.ceil((double) blocks.size() / cols);
        Panel[][] grid = new Panel[rows][cols];

        int col = 0;
        int row = 0;
        for (Block block : blocks) {
            Button button = new Button("");
            button.setPreferredSize(button.getPreferredSize().setX(64).setY(64));
            //button.setIcon(new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("/Textures/blocks/" + block.getName().replaceAll("\\s", "_") + ".png", false, false)));
            button.setIcon(new IconComponent("/Textures/blocks/" + block.getName().replaceAll("\\s", "_") + ".png"));
            button.addClickCommands(btn -> selectedBlock.setText(block.getName()));
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
