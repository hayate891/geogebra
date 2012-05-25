package geogebra.gui.dialog;

import geogebra.common.euclidian.AbstractEuclidianView;
import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.gui.InputHandler;
import geogebra.common.gui.dialog.handler.NumberChangeSignInputHandler;
import geogebra.common.gui.dialog.handler.NumberInputHandler;
import geogebra.common.gui.dialog.handler.RedefineInputHandler;
import geogebra.common.gui.dialog.handler.RenameInputHandler;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoBoolean;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoFunction;
import geogebra.common.kernel.geos.GeoNumeric;
import geogebra.common.kernel.geos.GeoPoint2;
import geogebra.common.kernel.geos.GeoPolygon;
import geogebra.common.kernel.geos.GeoSegment;
import geogebra.common.kernel.geos.GeoText;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.main.AbstractApplication;
import geogebra.gui.GuiManager;
import geogebra.gui.app.MyFileFilter;
import geogebra.gui.autocompletion.AutoCompletion;
import geogebra.gui.dialog.options.OptionsDialog;
import geogebra.gui.toolbar.ToolbarConfigDialog;
import geogebra.gui.util.GeoGebraFileChooser;
import geogebra.gui.view.functioninspector.FunctionInspector;
import geogebra.main.Application;
import geogebra.main.MyResourceBundle;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Class to manage all kind of dialogs, including the file chooser, appearing in
 * GeoGebra. Supports (explicit) lazy initialization so that dialogs have to be
 * created manually if needed.
 */
public class DialogManagerDesktop extends geogebra.common.gui.dialog.DialogManager {
	/**
	 * The option dialog where the user can change all application settings.
	 */
	private OptionsDialog optionsDialog;

	/**
	 * Object which provides an option dialog if requested. Used because
	 * different option dialogs are needed for GeoGebra 4 and 5.
	 */
	private OptionsDialog.Factory optionsDialogFactory;


	/**
	 * Dialog to view properties of a function.
	 */
	private FunctionInspector functionInspector;

	/**
	 * Dialog for styling text objects.
	 */
	private TextInputDialog textInputDialog;

	/**
	 * Dialog to select new files, either for loading or saving. Various file
	 * types are supported.
	 */
	private GeoGebraFileChooser fileChooser;

	/**
	 * Properties for translation of file chooser UI in languages Java doesn't
	 * support.
	 */
	private ResourceBundle rbJavaUI;

	/**
	 * Keep track of the current locale for file chooser UI updating.
	 */
	private Locale currentLocale;

	public DialogManagerDesktop(AbstractApplication app) {
		super(app);
	}

	/**
	 * Update the fonts used in the dialogs.
	 */
	public void updateFonts() {
		if (functionInspector != null)
			functionInspector.updateFonts();

		if (textInputDialog != null)
			textInputDialog.updateFonts();


		if (optionsDialog != null) {
			GuiManager.setFontRecursive(optionsDialog, ((Application) app).getPlainFont());
			SwingUtilities.updateComponentTreeUI(optionsDialog);
		}

		if (fileChooser != null) {
			fileChooser.setFont(((Application) app).getPlainFont());
			SwingUtilities.updateComponentTreeUI(fileChooser);
		}
	}

	/**
	 * Update labels in the GUI.
	 */
	public void setLabels() {

		if (optionsDialog != null)
			optionsDialog.setLabels();

		if (functionInspector != null)
			functionInspector.setLabels();

		if (textInputDialog != null)
			textInputDialog.setLabels();

		if (fileChooser != null)
			updateJavaUILanguage();
	}

	/**
	 * Displays the options dialog.
	 * 
	 * @param tabIndex
	 *            Index of the tab. Use OptionsDialog.TAB_* constants for this,
	 *            or -1 for the default, -2 to hide.
	 */
	@Override
	public void showOptionsDialog(int tabIndex) {
		if (optionsDialog == null)
			optionsDialog = optionsDialogFactory.create(((Application) app));
		else
			optionsDialog.updateGUI();

		if (tabIndex > -1)
			optionsDialog.showTab(tabIndex);

		optionsDialog.setVisible(tabIndex != -2);
	}

	/**
	 * Displays the properties dialog for geos
	 */
	@Override
	public void showPropertiesDialog(ArrayList<GeoElement> geos) {
		if (!((Application) app).letShowPropertiesDialog())
			return;


		AbstractApplication.debug("TODO : set option objects visible");
		if (geos != null && geos.size() == 1
				&& geos.get(0).isEuclidianVisible()
				&& geos.get(0) instanceof GeoNumeric)
			AbstractApplication.debug("TODO : propPanel.showSliderTab()");
	}

	private ArrayList<GeoElement> tempGeos = new ArrayList<GeoElement>();

	@Override
	public void showPropertiesDialog() {
		showPropertiesDialog(null);
	}

	/**
	 * Displays the configuration dialog for the toolbar
	 */
	@Override
	public void showToolbarConfigDialog() {
		app.getActiveEuclidianView().resetMode();
		ToolbarConfigDialog dialog = new ToolbarConfigDialog(((Application) app));
		dialog.setVisible(true);
	}

	/**
	 * Displays the rename dialog for geo
	 */
	@Override
	public void showRenameDialog(GeoElement geo, boolean storeUndo,
			String initText, boolean selectInitText) {
		if (!app.isRightClickEnabled())
			return;

		geo.setLabelVisible(true);
		geo.updateRepaint();

		InputHandler handler = new RenameInputHandler(app, geo, storeUndo);

		// Michael Borcherds 2008-03-25
		// a Chinese friendly version
		InputDialog id = new InputDialog(((Application) app), "<html>"
				+ app.getPlain("NewNameForA", "<b>" + geo.getNameDescription()
						+ "</b>") + // eg New name for <b>Segment a</b>
				"</html>", app.getPlain("Rename"), initText, false, handler,
				false, selectInitText, null);

		/*
		 * InputDialog id = new InputDialog( this, "<html>" +
		 * app.getPlain("NewName") + " " + app.getPlain("for") + " <b>" +
		 * geo.getNameDescription() + "</b></html>", app.getPlain("Rename"),
		 * initText, false, handler, true, selectInitText);
		 */

		id.setVisible(true);
	}

	/**
	 * Displays the redefine dialog for geo
	 * 
	 * @param allowTextDialog
	 *            whether text dialog should be used for texts
	 */
	@Override
	public void showRedefineDialog(GeoElement geo, boolean allowTextDialog) {
		if (allowTextDialog && geo.isGeoText() && !geo.isTextCommand()) {
			showTextDialog((GeoText) geo);
			return;
		}

		String str = geo.getRedefineString(false, true);

		InputHandler handler = new RedefineInputHandler(((Application) app), geo, str);

		InputDialog id = new InputDialog(((Application) app), geo.getNameDescription(),
				app.getPlain("Redefine"), str, true, handler, geo);
		id.showSymbolTablePopup(true);
		id.setVisible(true);
	}

	protected void showTextDialog(GeoText text, GeoPointND startPoint) {
		app.setWaitCursor();

		if (textInputDialog == null) {
			textInputDialog = (TextInputDialog) createTextDialog(text,
					startPoint);
		} else {
			textInputDialog.reInitEditor(text, startPoint);
		}

		textInputDialog.setVisible(true);
		app.setDefaultCursor();
	}

	public JDialog createTextDialog(GeoText text, GeoPointND startPoint) {
		boolean isTextMode = app.getMode() == EuclidianConstants.MODE_TEXT;
		TextInputDialog id = new TextInputDialog(((Application) app), app.getPlain("Text"),
				text, startPoint, 30, 6, isTextMode);
		return id;
	}

	/**
	 * Shows the function inspector dialog. If none exists, a new inspector is
	 * created.
	 */
	@Override
	public boolean showFunctionInspector(GeoFunction function) {
		boolean success = true;

		try {
			if (functionInspector == null) {
				functionInspector = new FunctionInspector(((Application) app), function);
			} else {
				functionInspector.insertGeoElement(function);
			}
			functionInspector.setVisible(true);

		} catch (Exception e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * Creates a new checkbox at given startPoint
	 */
	@Override
	public void showBooleanCheckboxCreationDialog(geogebra.common.awt.Point loc, GeoBoolean bool) {
		Point location = new Point(loc.x, loc.y);
		CheckboxCreationDialog d = new CheckboxCreationDialog(((Application) app), location, bool);
		d.setVisible(true);
	}

	/**
	 * Shows a modal dialog to enter a number or number variable name.
	 */
	@Override
	public NumberValue showNumberInputDialog(String title, String message,
			String initText) {
		// avoid labeling of num
		Construction cons = app.getKernel().getConstruction();
		boolean oldVal = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		InputDialog id = new InputDialog(((Application) app), message, title, initText, false,
				handler, true, false, null);
		id.setVisible(true);

		cons.setSuppressLabelCreation(oldVal);
		return handler.getNum();
	}

	/**
	 * Shows a modal dialog to enter a number or number variable name.
	 */
	@Override
	public NumberValue showNumberInputDialog(String title, String message,
			String initText, boolean changingSign, String checkBoxText) {
		// avoid labeling of num
		Construction cons = app.getKernel().getConstruction();
		boolean oldVal = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		NumberChangeSignInputHandler handler = new NumberChangeSignInputHandler(
				app.getKernel().getAlgebraProcessor());
		NumberChangeSignInputDialog id = new NumberChangeSignInputDialog(((Application) app),
				message, title, initText, handler, changingSign, checkBoxText);
		id.setVisible(true);

		cons.setSuppressLabelCreation(oldVal);

		return handler.getNum();
	}

	@Override
	public void showNumberInputDialogRegularPolygon(String title,
			GeoPoint2 geoPoint1, GeoPoint2 geoPoint2) {

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		InputDialog id = new InputDialogRegularPolygon(((Application) app), title, handler,
				geoPoint1, geoPoint2, app.getKernel());
		id.setVisible(true);

	}

	@Override
	public void showNumberInputDialogCirclePointRadius(String title,
			GeoPointND geoPoint1,  AbstractEuclidianView view) {

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		InputDialog id = new InputDialogCirclePointRadius(((Application) app), title, handler,
				(GeoPoint2) geoPoint1, app.getKernel());
		id.setVisible(true);

	}

	@Override
	public void showNumberInputDialogRotate(String title, GeoPolygon[] polys,
			GeoPoint2[] points, GeoElement[] selGeos) {

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		InputDialog id = new InputDialogRotate(((Application) app), title, handler, polys,
				points, selGeos, app.getKernel());
		id.setVisible(true);

	}

	@Override
	public void showNumberInputDialogAngleFixed(String title,
			GeoSegment[] segments, GeoPoint2[] points, GeoElement[] selGeos) {

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		InputDialog id = new InputDialogAngleFixed(((Application) app), title, handler,
				segments, points, selGeos, app.getKernel());
		id.setVisible(true);

	}

	@Override
	public void showNumberInputDialogDilate(String title, GeoPolygon[] polys,
			GeoPoint2[] points, GeoElement[] selGeos) {

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		InputDialog id = new InputDialogDilate(((Application) app), title, handler, points,
				selGeos, app.getKernel());
		id.setVisible(true);

	}

	@Override
	public void showNumberInputDialogSegmentFixed(String title,
			GeoPoint2 geoPoint1) {

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		InputDialog id = new InputDialogSegmentFixed(((Application) app), title, handler,
				geoPoint1, app.getKernel());
		id.setVisible(true);

	}

	/**
	 * Shows a modal dialog to enter an angle or angle variable name.
	 * 
	 * @return: Object[] with { NumberValue, AngleInputDialog } pair
	 */
	@Override
	public Object[] showAngleInputDialog(String title, String message,
			String initText) {
		// avoid labeling of num
		Construction cons = app.getKernel().getConstruction();
		boolean oldVal = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		NumberInputHandler handler = new NumberInputHandler(app.getKernel()
				.getAlgebraProcessor());
		AngleInputDialog id = new AngleInputDialog(((Application) app), message, title,
				initText, false, handler, true);
		id.setVisible(true);

		cons.setSuppressLabelCreation(oldVal);
		Object[] ret = { handler.getNum(), id };
		return ret;
	}

	/**
	 * Close all open dialogs.
	 * 
	 */
	@Override
	public void closeAll() {
		//closePropertiesDialog();
	}


	/**
	 * Creates a new slider at given location (screen coords).
	 * 
	 * @return whether a new slider (number) was create or not
	 */
	@Override
	public boolean showSliderCreationDialog(int x, int y) {
		app.setWaitCursor();

		SliderDialog dialog = new SliderDialog(((Application) app), x, y);
		dialog.setVisible(true);

		app.setDefaultCursor();

		return true;
	}

	/**
	 * Creates a new JavaScript button at given location (screen coords).
	 * 
	 * @return whether a new slider (number) was create or not
	 */
	@Override
	public boolean showButtonCreationDialog(int x, int y, boolean textfield) {
		ButtonDialog dialog = new ButtonDialog(((Application) app), x, y, textfield);
		dialog.setVisible(true);
		return true;
	}


	public synchronized void initFileChooser() {
		if (fileChooser == null) {
			try {
				setFileChooser(new GeoGebraFileChooser(((Application) app),
						((Application) app).getCurrentImagePath())); // non-restricted
				fileChooser.addPropertyChangeListener(
						JFileChooser.FILE_FILTER_CHANGED_PROPERTY,
						new FileFilterChangedListener());
			} catch (Exception e) {
				// fix for java.io.IOException: Could not get shell folder ID
				// list
				// Java bug
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
				AbstractApplication
						.debug("Error creating GeoGebraFileChooser - using fallback option");
				setFileChooser(new GeoGebraFileChooser(((Application) app),
						((Application) app).getCurrentImagePath(), true)); // restricted version
			}

			updateJavaUILanguage();
		}
	}

	/**
	 * Loads java-ui.properties and sets all key-value pairs using
	 * UIManager.put(). This is needed to translate JFileChooser to languages
	 * not supported by Java natively.
	 */
	private void updateJavaUILanguage() {
		// load properties jar file
		if (currentLocale == ((Application) app).getLocale())
			return;

		// update locale
		currentLocale = ((Application) app).getLocale();
		String lang = currentLocale.getLanguage();
		boolean deleteKeys = false;

		if ("it".equals(lang)
				|| "zh".equals(lang)
				|| "ja".equals(lang)
				|| "de".equals(lang)
				//|| "es".equals(lang) we have our own Spanish translation
				//|| "fr".equals(lang) we have our own French translation
				|| "ko".equals(lang)
				|| "sv".equals(lang) ) {
			// get keys to delete
			// as Java is localized in these languages already
			// http://openjdk.java.net/groups/i18n/
			rbJavaUI = MyResourceBundle
					.loadSingleBundleFile(Application.RB_JAVA_UI);		
			deleteKeys = true;
		} else {
			rbJavaUI = MyResourceBundle.createBundle(Application.RB_JAVA_UI, currentLocale);			
		}
		
		Enumeration<String> keys = rbJavaUI.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = deleteKeys ? null : rbJavaUI.getString(key);
			UIManager.put(key, value);
		}

		// update file chooser
		if (getFileChooser() != null) {
			getFileChooser().setLocale(currentLocale);
			SwingUtilities.updateComponentTreeUI(getFileChooser());

			// Unfortunately the preceding line removes the event listener from
			// the
			// internal JTextField inside the file chooser. This means that the
			// listener has to be registered again. (e.g. a simple call to
			// 'AutoCompletion.install(this);' inside the GeoGebraFileChooser
			// constructor is not sufficient)
			AutoCompletion.install(getFileChooser(), true);
		}
	}

	public OptionsDialog getOptionsDialog() {
		return optionsDialog;
	}


	public GeoGebraFileChooser getFileChooser() {
		return fileChooser;
	}

	public void setFileChooser(GeoGebraFileChooser fileChooser) {
		this.fileChooser = fileChooser;
	}

	public FunctionInspector getFunctionInspector() {
		return functionInspector;
	}

	public TextInputDialog getTextInputDialog() {
		return textInputDialog;
	}

	public OptionsDialog.Factory getOptionsDialogFactory() {
		return optionsDialogFactory;
	}

	public void setOptionsDialogFactory(
			OptionsDialog.Factory optionsDialogFactory) {
		this.optionsDialogFactory = optionsDialogFactory;
	}

	/*
	 * PropertyChangeListener implementation to handle file filter changes
	 */
	private class FileFilterChangedListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (getFileChooser().getFileFilter() instanceof geogebra.gui.app.MyFileFilter) {
				String fileName = null;
				if (getFileChooser().getSelectedFile() != null) {
					fileName = getFileChooser().getSelectedFile().getName();
				}

				// fileName = getFileName(fileName);

				if (fileName != null && fileName.indexOf(".") > -1) {
					fileName = fileName.substring(0, fileName.lastIndexOf("."))
							+ "."
							+ ((MyFileFilter) getFileChooser().getFileFilter())
									.getExtension();

					getFileChooser().setSelectedFile(
							new File(getFileChooser().getCurrentDirectory(),
									fileName));
				}
			}
		}
	}

	/**
	 * Factory for the {@link DialogManagerDesktop} class.
	 */
	public static class Factory {
		/**
		 * @param app
		 *            Application instance
		 * @return a new {@link DialogManagerDesktop}
		 */
		public DialogManagerDesktop create(Application app) {
			DialogManagerDesktop dialogManager = new DialogManagerDesktop(app);
			dialogManager.setOptionsDialogFactory(new OptionsDialog.Factory());
			return dialogManager;
		}
	}

	@Override
	protected String prompt(String message, String def) {
		Application.debug("Shouldn't ever be called");
		return null;
	}

	@Override
	protected boolean confirm(String string) {
		Application.debug("Shouldn't ever be called");
		return false;
	}



}
