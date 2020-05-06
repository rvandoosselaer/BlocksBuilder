package com.rvandoosselaer.blocksbuilder.gui;

import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.RangedValueModel;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.style.ElementId;

/**
 * A GridPanel implementation that adds a vertical slider to scroll through the grid.
 *
 * @author: rvandoosselaer
 */
public class ExtendedGridPanel extends Container {

    public static final String ELEMENT_ID = "extendedGridPanel";

    private Slider slider;
    private GridPanel gridPanel;
    private RangedValueModel indexModel;
    private VersionedReference<Double> indexModelRef;
    private int maxIndex;
    private int minIndex = 0;

    public ExtendedGridPanel(GridModel<Panel> model) {
        this(model, Math.max(0, Math.min(5, model.getRowCount())), model.getColumnCount());
    }

    public ExtendedGridPanel(GridModel<Panel> model, int visibleRows, int visibleColumns) {
        super(new BorderLayout(), new ElementId(ELEMENT_ID));

        gridPanel = addChild(new GridPanel(model), BorderLayout.Position.Center);
        gridPanel.setVisibleSize(visibleRows, visibleColumns);

        int rows = model.getRowCount();
        maxIndex = Math.max(0, rows - visibleRows);

        indexModel = new DefaultRangedValueModel();
        indexModelRef = indexModel.createReference();

        slider = addChild(new Slider(indexModel, Axis.Y), BorderLayout.Position.East);

        refreshSliderIndex();
        indexModel.setValue(maxIndex);
    }

    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        if (indexModelRef.update()) {
            int index = (int) (maxIndex - indexModel.getValue());
            gridPanel.setRow(index);
        }

    }

    public void setModel(GridModel<Panel> model) {
        gridPanel.setModel(model);
        refreshSliderIndex();
        indexModel.setValue(maxIndex);
    }

    public void setVisibleSize(int rows, int cols) {
        gridPanel.setVisibleSize(rows, cols);
        refreshSliderIndex();
    }

    private void refreshSliderIndex() {
        int count = gridPanel.getModel().getRowCount();
        int visible = gridPanel.getVisibleRows();
        maxIndex = Math.max(0, count - visible);

        // Because the slider is upside down, we have to
        // do some math if we want our base not to move as
        // items are added to the list after us
        double val = indexModel.getMaximum() - indexModel.getValue();

        indexModel.setMinimum(minIndex);
        indexModel.setMaximum(maxIndex);
        indexModel.setValue(maxIndex - val);
    }

}
