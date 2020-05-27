package com.rvandoosselaer.blocksbuilder.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.rvandoosselaer.blocks.Block;
import com.rvandoosselaer.blocks.BlockIds;
import com.rvandoosselaer.blocks.BlockRegistry;
import com.rvandoosselaer.blocks.BlocksConfig;
import com.rvandoosselaer.blocks.TypeIds;
import com.rvandoosselaer.blocksbuilder.BuilderBlock;
import com.rvandoosselaer.blocksbuilder.BuilderState;
import com.rvandoosselaer.jmeutils.gui.GuiUtils;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.focus.FocusChangeEvent;
import com.simsilica.lemur.focus.FocusChangeListener;
import com.simsilica.lemur.grid.ArrayGridModel;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An AppState that renders the blocks window.
 *
 * @author: rvandoosselaer
 */
@Slf4j
public class BlocksState extends BaseAppState {

    private static final int COLS = 4;
    @Getter
    @Setter
    private Node node;
    private Container blocksContainer;
    private ExtendedGridPanel blocksGrid;
    private String filterPlaceholderText = "Filter...";
    private VersionedReference<DocumentModel> filterRef;
    private Label selectedBlockLabel;
    private Button selectedBlockImage;
    private BuilderState builderState;
    private VersionedReference<BuilderBlock> selectedBlockRef;
    private GridPanel recentlyUsedBlocksGrid;
    private List<BuilderBlock> blocks;

    @Override
    protected void initialize(Application app) {
        builderState = getState(BuilderState.class);
        // set the default block
        selectedBlockRef = builderState.getSelectedBlock().createReference();
        builderState.setSelectedBlock(getDefaultBlock());
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

        // update the filter
        if (filterRef.update()) {
            filterBlocks(filterRef.get().getText());
        }

        // update the selected block
        if (selectedBlockRef.update()) {
            selectedBlockLabel.setText(selectedBlockRef.get().getName());
            ((IconComponent) selectedBlockImage.getIcon()).setImageTexture(GuiGlobals.getInstance().loadTexture(getIconPath(selectedBlockRef.get().getBlock()), false, false));
        }
    }

    public Optional<Block> getRotatedBlock(Block block) {
        for (BuilderBlock builderBlock : getBlocks()) {
            Optional<Block> nextBlock = builderBlock.getNextBlock(block);
            if (nextBlock.isPresent()) {
                return nextBlock;
            }
        }

        return Optional.empty();
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
        List<BuilderBlock> filteredBlocks = new ArrayList<>(getBlocks());
        for (String s : split) {
            filteredBlocks.removeAll(getBlocks().stream()
                    .filter(block -> !block.getName().contains(s))
                    .collect(Collectors.toList()));
        }

        blocksGrid.setModel(new ArrayGridModel<>(createBlocksGridArray(filteredBlocks, COLS)));
    }

    private Container createBlocksContainer() {
        Container container = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Even));
        container.addChild(new Label("Blocks", new ElementId("title")));

        Container filterWrapper = container.addChild(new Container(new BorderLayout(), new ElementId("wrapper")));
        TextField filter = filterWrapper.addChild(new TextField("Filter..."), BorderLayout.Position.Center);
        filter.setTextVAlignment(VAlignment.Center);
        filter.getControl(GuiControl.class).addFocusChangeListener(new FilterTextFieldFocusListener(filterPlaceholderText));
        filterRef = filter.getDocumentModel().createReference();
        Button clearFilter = filterWrapper.addChild(new Button("Clear"), BorderLayout.Position.East);
        clearFilter.addClickCommands(source -> clearFilter(filter));

        blocksGrid = container.addChild(new ExtendedGridPanel(new ArrayGridModel<>(createBlocksGridArray(getBlocks(), COLS))));

        container.addChild(new Label("Selected block:", new ElementId("title")));
        Container selectedBlockWrapper = container.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.First, FillMode.Even), new ElementId("wrapper")));
        selectedBlockLabel = selectedBlockWrapper.addChild(new Label(selectedBlockRef.get().getName(), new ElementId(Label.ELEMENT_ID).child("value.label")));
        selectedBlockLabel.setTextHAlignment(HAlignment.Left);
        selectedBlockImage = selectedBlockWrapper.addChild(new Button(""));
        selectedBlockImage.setIcon(getBlockIcon(selectedBlockRef.get().getBlock()));
        selectedBlockImage.setEnabled(false);

        container.addChild(new Label("Recently used blocks:", new ElementId("title")));
        recentlyUsedBlocksGrid = container.addChild(new GridPanel(new ArrayGridModel<>(new Panel[1][4])));
        recentlyUsedBlocksGrid.setVisibleSize(1, 4);

        return container;
    }

    private void clearFilter(TextField filter) {
        filter.setText(filterPlaceholderText);
        blocksGrid.setModel(new ArrayGridModel<>(createBlocksGridArray(getBlocks(), COLS)));
    }

    private BuilderBlock getDefaultBlock() {
        for (BuilderBlock builderBlock : getBlocks()) {
            if (builderBlock.getName().equals(BlockIds.getName(TypeIds.GRASS, "cube"))) {
                return builderBlock;
            }
        }
        return null;
    }

    private List<BuilderBlock> getBlocks() {
        if (blocks == null) {
            blocks = new ArrayList<>();

            BlockRegistry blockRegistry = BlocksConfig.getInstance().getBlockRegistry();
            for (Block block : blockRegistry.getAll()) {
                String genericShape = block.getShape();
                int underscoreIndex = genericShape.lastIndexOf("_");
                if (underscoreIndex > 0) {
                    genericShape = genericShape.substring(0, underscoreIndex);
                }
                String name = block.getType() + "-" + genericShape;

                // find the matching BuilderBlock
                BuilderBlock builderBlock = blocks.stream().filter(b -> name.equals(b.getName())).findFirst().orElse(new BuilderBlock(name));
                if (builderBlock.getBlock() == null) {
                    // no previous builder block was found
                    blocks.add(builderBlock);
                }
                builderBlock.addBlock(block);
            }

            blocks.sort(Comparator.comparing(BuilderBlock::getName));
        }

        return blocks;
    }

    private Panel[][] createBlocksGridArray(Collection<BuilderBlock> blocks, int cols) {
        int rows = (int) Math.ceil((double) blocks.size() / cols);
        Panel[][] grid = new Panel[rows][cols];

        int col = 0;
        int row = 0;
        for (BuilderBlock builderBlock : blocks) {
            Button button = getBlockButton(builderBlock);
            grid[row][col++] = button;

            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    private void onSelectBlock(BuilderBlock builderBlock) {
        builderState.setSelectedBlock(builderBlock);
        // reset the builder block to use the default shape again
        builderBlock.reset();

        // update the recently used block list
        GridModel<Panel> model = recentlyUsedBlocksGrid.getModel();
        Panel firstItem = model.getCell(0, 0, null);
        if (firstItem == null) {
            // no previous block, set the block as first and fill the list with empty spots
            model.setCell(0, 0, getBlockButton(builderBlock));
            for (int col = 1; col < model.getColumnCount(); col++) {
                Button dummyButton = new Button("");
                dummyButton.setEnabled(false);
                model.setCell(0, col, dummyButton);
            }
            return;
        }

        // create a list from the array, it's easier to pop and move items this way.
        List<Panel> blocks = new ArrayList<>();
        for (int col = 0; col < model.getColumnCount(); col++) {
            blocks.add(model.getCell(0, col, null));
        }

        // check if the block is already in the list
        Optional<Panel> optionalBlock = blocks.stream()
                .filter(b -> builderBlock.getName().equals(b.getUserData("id")))
                .findFirst();

        if (optionalBlock.isPresent()) {
            // block is already in the list, move it to the first position
            blocks.remove(optionalBlock.get());
            blocks.add(0, optionalBlock.get());
        } else {
            blocks.add(0, getBlockButton(builderBlock));
        }

        // recreate the grid from the list
        for (int col = 0; col < model.getColumnCount(); col++) {
            model.setCell(0, col, blocks.get(col));
        }
    }

    private Button getBlockButton(BuilderBlock builderBlock) {
        Button button = new Button("");
        IconComponent icon = getBlockIcon(builderBlock.getBlock());
        button.setIcon(icon);
        button.addClickCommands(btn -> onSelectBlock(builderBlock));
        button.setUserData("id", builderBlock.getName());

        return button;
    }

    private IconComponent getBlockIcon(Block block) {
        IconComponent icon = new IconComponent(getIconPath(block));
        icon.setHAlignment(HAlignment.Center);
        icon.setVAlignment(VAlignment.Center);
        icon.setIconSize(new Vector2f(50, 50));
        return icon;
    }

    private String getIconPath(Block block) {
        return "/Textures/blocks/" + block.getName() + ".png";
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
