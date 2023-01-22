package sporemodder.userinterface.smfxlook;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;
import java.awt.*;

import sun.awt.AppContext;
import sun.security.action.GetPropertyAction;
import sun.swing.SwingUtilities2;

/**
 * A concrete implementation of {@code MetalTheme} providing
 * the original look of the Java Look and Feel, code-named "Steel". Refer
 * to {@link MetalLookAndFeel#setCurrentTheme} for details on changing
 * the default theme.
 * <p>
 * All colors returned by {@code DefaultMetalTheme} are completely
 * opaque.
 *
 * <h3><a name="fontStyle"></a>Font Style</h3>
 *
 * {@code DefaultMetalTheme} uses bold fonts for many controls.  To make all
 * controls (with the exception of the internal frame title bars and
 * client decorated frame title bars) use plain fonts you can do either of
 * the following:
 * <ul>
 * <li>Set the system property <code>swing.boldMetal</code> to
 *     <code>false</code>.  For example,
 *     <code>java&nbsp;-Dswing.boldMetal=false&nbsp;MyApp</code>.
 * <li>Set the defaults property <code>swing.boldMetal</code> to
 *     <code>Boolean.FALSE</code>.  For example:
 *     <code>UIManager.put("swing.boldMetal",&nbsp;Boolean.FALSE);</code>
 * </ul>
 * The defaults property <code>swing.boldMetal</code>, if set,
 * takes precedence over the system property of the same name. After
 * setting this defaults property you need to re-install
 * <code>MetalLookAndFeel</code>, as well as update the UI
 * of any previously created widgets. Otherwise the results are undefined.
 * The following illustrates how to do this:
 * <pre>
 *   // turn off bold fonts
 *   UIManager.put("swing.boldMetal", Boolean.FALSE);
 *
 *   // re-install the Metal Look and Feel
 *   UIManager.setLookAndFeel(new MetalLookAndFeel());
 *
 *   // Update the ComponentUIs for all Components. This
 *   // needs to be invoked for all windows.
 *   SwingUtilities.updateComponentTreeUI(rootComponent);
 * </pre>
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans&trade;
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @see MetalLookAndFeel
 * @see MetalLookAndFeel#setCurrentTheme
 *
 * @author Steve Wilson
 */
public class DarkMetalTheme2 extends DarkMetalThemeBase {
    /**
     * Whether or not fonts should be plain.  This is only used if
     * the defaults property 'swing.boldMetal' == "false".
     */
    private static final boolean PLAIN_FONTS;

    /**
     * Names of the fonts to use.
     */
    private static final String[] fontNames = {
        Font.DIALOG,Font.DIALOG,Font.DIALOG,Font.DIALOG,Font.DIALOG,Font.DIALOG
    };
    /**
     * Styles for the fonts.  This is ignored if the defaults property
     * <code>swing.boldMetal</code> is false, or PLAIN_FONTS is true.
     */
    private static final int[] fontStyles = {
        Font.BOLD, Font.PLAIN, Font.PLAIN, Font.BOLD, Font.BOLD, Font.PLAIN
    };
    /**
     * Sizes for the fonts.
     */
    private static final int[] fontSizes = {
        12, 12, 12, 12, 12, 10
    };

    // note the properties listed here can currently be used by people
    // providing runtimes to hint what fonts are good.  For example the bold
    // dialog font looks bad on a Mac, so Apple could use this property to
    // hint at a good font.
    //
    // However, we don't promise to support these forever.  We may move
    // to getting these from the swing.properties file, or elsewhere.
    /**
     * System property names used to look up fonts.
     */
    private static final String[] defaultNames = {
        "swing.plaf.metal.controlFont",
        "swing.plaf.metal.systemFont",
        "swing.plaf.metal.userFont",
        "swing.plaf.metal.controlFont",
        "swing.plaf.metal.controlFont",
        "swing.plaf.metal.smallFont"
    };

    /**
     * Returns the ideal font name for the font identified by key.
     */
    static String getDefaultFontName(int key) {
        return fontNames[key];
    }

    /**
     * Returns the ideal font size for the font identified by key.
     */
    static int getDefaultFontSize(int key) {
        return fontSizes[key];
    }

    /**
     * Returns the ideal font style for the font identified by key.
     */
    static int getDefaultFontStyle(int key) {
        if (key != WINDOW_TITLE_FONT) {
            Object boldMetal = null;
            if (AppContext.getAppContext().get(
                    SwingUtilities2.LAF_STATE_KEY) != null) {
                // Only access the boldMetal key if a look and feel has
                // been loaded, otherwise we'll trigger loading the look
                // and feel.
                boldMetal = UIManager.get("swing.boldMetal");
            }
            if (boldMetal != null) {
                if (Boolean.FALSE.equals(boldMetal)) {
                    return Font.PLAIN;
                }
            }
            else if (PLAIN_FONTS) {
                return Font.PLAIN;
            }
        }
        return fontStyles[key];
    }

    /**
     * Returns the default used to look up the specified font.
     */
    static String getDefaultPropertyName(int key) {
        return defaultNames[key];
    }

    static {
        Object boldProperty = java.security.AccessController.doPrivileged(
            new GetPropertyAction("swing.boldMetal"));
        if (boldProperty == null || !"false".equals(boldProperty)) {
            PLAIN_FONTS = false;
        }
        else {
            PLAIN_FONTS = true;
        }
    }

    private static final ColorUIResource primary1 = new ColorUIResource(
                              102, 102, 153);
    private static final ColorUIResource primary2 = new ColorUIResource(153,
                              153, 204);
    private static final ColorUIResource primary3 = new ColorUIResource(
                              204, 204, 255);
    private static final ColorUIResource secondary1 = new ColorUIResource(
                              102, 102, 102);
    private static final ColorUIResource secondary2 = new ColorUIResource(
                              153, 153, 153);
    private static final ColorUIResource secondary3 = new ColorUIResource(
                              204, 204, 204);

    /**
     * Returns the name of this theme. This returns {@code "Steel"}.
     *
     * @return the name of this theme.
     */
    public String getName() { return "Steel"; }

    /**
     * Creates and returns an instance of {@code DarkMetalTheme2}.
     */
    public DarkMetalTheme2() {
        super();
    }

    /**
     * Returns the primary 1 color. This returns a color with rgb values
     * of 102, 102, and 153, respectively.
     *
     * @return the primary 1 color
     */
    protected ColorUIResource getPrimary1() { return primary1; }

    /**
     * Returns the primary 2 color. This returns a color with rgb values
     * of 153, 153, 204, respectively.
     *
     * @return the primary 2 color
     */
    protected ColorUIResource getPrimary2() { return primary2; }

    /**
     * Returns the primary 3 color. This returns a color with rgb values
     * 204, 204, 255, respectively.
     *
     * @return the primary 3 color
     */
    protected ColorUIResource getPrimary3() { return primary3; }

    /**
     * Returns the secondary 1 color. This returns a color with rgb values
     * 102, 102, and 102, respectively.
     *
     * @return the secondary 1 color
     */
    protected ColorUIResource getSecondary1() { return secondary1; }

    /**
     * Returns the secondary 2 color. This returns a color with rgb values
     * 153, 153, and 153, respectively.
     *
     * @return the secondary 2 color
     */
    protected ColorUIResource getSecondary2() { return secondary2; }

    /**
     * Returns the secondary 3 color. This returns a color with rgb values
     * 204, 204, and 204, respectively.
     *
     * @return the secondary 3 color
     */
    protected ColorUIResource getSecondary3() { return secondary3; }
}