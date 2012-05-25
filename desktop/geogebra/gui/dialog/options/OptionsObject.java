/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

package geogebra.gui.dialog.options;

import geogebra.common.gui.SetLabels;
import geogebra.common.kernel.ConstructionDefaults;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.gui.color.GeoGebraColorChooser;
import geogebra.gui.dialog.PropertiesPanel;
import geogebra.gui.view.algebra.AlgebraController;
import geogebra.gui.view.algebra.AlgebraTree;
import geogebra.gui.view.algebra.AlgebraTreeController;
import geogebra.main.Application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * @author Markus Hohenwarter
 */
public class OptionsObject extends JPanel implements OptionPanel, SetLabels {

	// private static final int MAX_OBJECTS_IN_TREE = 500;
	private static final int MAX_GEOS_FOR_EXPAND_ALL = 15;
	private static final int MAX_COMBOBOX_ENTRIES = 200;
	private static final int MIN_LIST_WIDTH = 120;

	private static final long serialVersionUID = 1L;
	private Application app;
	private Kernel kernel;
	private JButton defaultsButton, delButton;
	private PropertiesPanel propPanel;
	private GeoGebraColorChooser colChooser;
	
	private AlgebraTree tree;

	private JSplitPane splitPane;
	private JScrollPane listScroller;

	// stop slider increment being less than 0.00000001
	public final static int TEXT_FIELD_FRACTION_DIGITS = 8;
	public final static int SLIDER_MAX_WIDTH = 170;

	final private static int MIN_WIDTH = 500;
	final private static int MIN_HEIGHT = 300;

	/**
	 * Creates new PropertiesDialog.
	 * 
	 * @param app
	 *            parent frame
	 */
	public OptionsObject(Application app) {
		
		this.app = app;
		kernel = app.getKernel();


		// build GUI
		initGUI();
	}

	/**
	 * inits GUI with labels of current language
	 */
	public void initGUI() {

		boolean wasShowing = isShowing();
		if (wasShowing) {
			setVisible(false);
		}
		
		// LIST PANEL
		tree = new AlgebraTree(new AlgebraTreeController(kernel));
		listScroller = new JScrollPane(tree);
		listScroller.setMinimumSize(new Dimension(MIN_LIST_WIDTH, 200));
		listScroller.setBackground(Color.white);
		listScroller.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));


		// delete button
		delButton = new JButton(app.getImageIcon("delete_small.gif"));
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedGeos();
			}
		});

		// apply defaults button
		defaultsButton = new JButton();
		defaultsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyDefaults();
			}
		});



		// build button panel with some buttons on the left
		// and some on the right
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
		buttonPanel.add(leftButtonPanel, BorderLayout.WEST);

		// left buttons
		leftButtonPanel.add(defaultsButton);
		

		// right buttons
		if (app.letDelete())
			rightButtonPanel.add(delButton);


		// PROPERTIES PANEL
		if (colChooser == null) {
			// init color chooser
			colChooser = new GeoGebraColorChooser(app);
		}

		// check for null added otherwise you get two listeners for the
		// colChooser
		// when a file is loaded
		if (propPanel == null) {
			propPanel = new PropertiesPanel(app, colChooser, false);
			propPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		}

		// put it all together
		this.removeAll();
		// contentPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		splitPane = new JSplitPane();
		splitPane.setLeftComponent(listScroller);
		splitPane.setRightComponent(propPanel);

		this.setLayout(new BorderLayout());
		//this.add(propPanel, BorderLayout.CENTER);
		this.add(splitPane, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		

		if (wasShowing) {
			setVisible(true);
		}

		setLabels();

	}



	public PropertiesPanel getPropertiesPanel() {
		return propPanel;
	}

	public void showSliderTab() {
		if (propPanel != null)
			propPanel.showSliderTab();
	}

	/**
	 * Update the labels of this dialog.
	 * 
	 * TODO Create "Apply Defaults" phrase (F.S.)
	 */
	public void setLabels() {
		
		delButton.setText(app.getPlain("Delete"));
		defaultsButton.setText(app.getMenu("ApplyDefaults"));
		
		propPanel.setLabels();
		
	}

	


	/**
	 * Reset the visual style of the selected elements.
	 * 
	 * TODO Does not work with lists (F.S.)
	 */
	private void applyDefaults() {

		ConstructionDefaults defaults = kernel.getConstruction()
				.getConstructionDefaults();
		
		for (GeoElement geo : app.getSelectedGeos()){
			defaults.setDefaultVisualStyles(geo, true);
			geo.updateRepaint();
		}

	}
	
	
	

	/**
	 * shows this dialog and select GeoElement geo at screen position location
	 */
	public void setVisibleWithGeos(ArrayList<GeoElement> geos) {
		kernel.clearJustCreatedGeosInViews();

		setViewActive(true);


		if (!isShowing()) {
			// pack and center on first showing
			if (firstTime) {
				// TODO ---- is this needed?
				//pack();
				//setLocationRelativeTo(app.getMainComponent());
				firstTime = false;
			}

			// ensure min size
			Dimension dim = getSize();
			if (dim.width < MIN_WIDTH) {
				dim.width = MIN_WIDTH;
				setSize(dim);
			}
			if (dim.height < MIN_HEIGHT) {
				dim.height = MIN_HEIGHT;
				setSize(dim);
			}

			super.setVisible(true);
		}
	}

	private boolean firstTime = true;

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			setVisibleWithGeos(null);
		} else {
			super.setVisible(false);
			setViewActive(false);
		}
	}

	private void setViewActive(boolean flag) {
		if (flag == viewActive)
			return;
		viewActive = flag;

	}

	private boolean viewActive = false;




	/**
	 * deletes all selected GeoElements from Kernel
	 */
	private void deleteSelectedGeos() {
		
		app.deleteSelectedObjects();
		
	}

	
	private int dividerLocation = MIN_LIST_WIDTH;

	/**
	 * show the geo list
	 */
	public void setGeoTreeVisible() {
		splitPane.setDividerSize(8);
		splitPane.setDividerLocation(dividerLocation);
		listScroller.setVisible(true);
		splitPane.repaint();
		
	}

	/**
	 * hide the geo list
	 */
	public void setGeoTreeNotVisible() {
		
		listScroller.setVisible(false);
		dividerLocation=splitPane.getDividerLocation();
		splitPane.setDividerSize(0);
		splitPane.repaint();
		
	}
	
	/**
	 * update selection regarding Application
	 */
	public void updateSelection() {
		updateSelection(app.getSelectedGeos().toArray());
	}
	
	private void updateSelection(Object[] geos) {
		// if (geos == oldSelGeos) return;
		// oldSelGeos = geos;

		
		//Application.printStacktrace("");
		propPanel.updateSelection(geos);
	}
	
	public void updateOneGeoDefinition(GeoElement geo) {
		
		propPanel.updateOneGeoDefinition(geo);
	}
	

	/**
	 * @return the tree
	 */
	public AlgebraTree getTree(){
		return tree;
	}

	public void updateGUI() {
		setLabels();
		
	}
	

} // PropertiesDialog