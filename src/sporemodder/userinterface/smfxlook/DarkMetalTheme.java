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
 * All colors returned by {@code DarkMetalTheme} are completely
 * opaque.
 *
 * <h3><a name="fontStyle"></a>Font Style</h3>
 *
 * {@code DarkMetalTheme} uses bold fonts for many controls.  To make all
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
public class DarkMetalTheme extends DefaultMetalTheme {
/*
    /**
     * Returns the default used to look up the specified font.
     * /
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
 	*/
	
    private static final ColorUIResource primary1 = new ColorUIResource(
    		22
    		, 22
    		, 73
    		/*
    		102
    		, 102
    		, 153
    		*/
	);
    private static final ColorUIResource primary2 = new ColorUIResource(
    		73
    		, 73
    		, 124
    		/*
    		153
    		, 153
    		, 204
    		*/
	);
    private static final ColorUIResource primary3 = new ColorUIResource(
    		124
    		, 124
    		, 175
    		/*
    		204
    		, 204
    		, 255
    		*/
	);
    private static final ColorUIResource secondary1 = new ColorUIResource(
    		22
    		, 22
    		, 22
    		/*
    		102
    		, 102
    		, 102
    		*/
	);
    private static final ColorUIResource secondary2 = new ColorUIResource(
    		73
    		, 73
    		, 73
    		/*
    		153
    		, 153
    		, 153
    		*/
	);
    private static final ColorUIResource secondary3 = new ColorUIResource(
    		124
    		, 124
    		, 124
    		/*
    		204
    		, 204
    		, 204
    		*/
	);

    
    /*
    /**
     * Returns the white color. This returns opaque black
     * ({@code 0xFF000000}).
     *
     * @return the white color
     * /
    @Override
    protected ColorUIResource getWhite() { return super.getBlack(); }

    /**
     * Returns the black color. This returns opaque white
     * ({@code 0xFFFFFFFF}).
     *
     * @return the black color
     * /
    @Override
    protected ColorUIResource getBlack() { return super.getWhite(); }
    //private FontDelegate fontDelegate;
    */
    
    /**
     * Returns the focus color. This returns the value of
     * {@code getPrimary2()}.
     *
     * @return the focus color
     */
    @Override
    public ColorUIResource getFocusColor() { return getPrimary2(); }

    /**
     * Returns the desktop color. This returns the value of
     * {@code getPrimary2()}.
     *
     * @return the desktop color
     */
    @Override
    public  ColorUIResource getDesktopColor() { return getPrimary2(); }

    /**
     * Returns the control color. This returns the value of
     * {@code getSecondary3()}.
     *
     * @return the control color
     */
    @Override
    public ColorUIResource getControl() { return getSecondary3(); }

    /**
     * Returns the control shadow color. This returns
     * the value of {@code getSecondary2()}.
     *
     * @return the control shadow color
     */
    @Override
    public ColorUIResource getControlShadow() { return getSecondary2(); }

    /**
     * Returns the control dark shadow color. This returns
     * the value of {@code getSecondary1()}.
     *
     * @return the control dark shadow color
     */
    @Override
    public ColorUIResource getControlDarkShadow() { return getSecondary1(); }

    /**
     * Returns the control info color. This returns
     * the value of {@code getWhite()}.
     *
     * @return the control info color
     */
    @Override
    public ColorUIResource getControlInfo() { return getWhite(); }

    /**
     * Returns the control highlight color. This returns
     * the value of {@code getBlack()}.
     *
     * @return the control highlight color
     */
    @Override
    public ColorUIResource getControlHighlight() { return getBlack(); }

    /**
     * Returns the control disabled color. This returns
     * the value of {@code getSecondary2()}.
     *
     * @return the control disabled color
     */
    @Override
    public ColorUIResource getControlDisabled() { return getSecondary2(); }

    /**
     * Returns the primary control color. This returns
     * the value of {@code getPrimary3()}.
     *
     * @return the primary control color
     */
    @Override
    public ColorUIResource getPrimaryControl() { return getPrimary3(); }

    /**
     * Returns the primary control shadow color. This returns
     * the value of {@code getPrimary2()}.
     *
     * @return the primary control shadow color
     */
    @Override
    public ColorUIResource getPrimaryControlShadow() { return getPrimary2(); }
    /**
     * Returns the primary control dark shadow color. This
     * returns the value of {@code getPrimary1()}.
     *
     * @return the primary control dark shadow color
     */
    @Override
    public ColorUIResource getPrimaryControlDarkShadow() { return getPrimary1(); }

    /**
     * Returns the primary control info color. This
     * returns the value of {@code getWhite()}.
     *
     * @return the primary control info color
     */
    @Override
    public ColorUIResource getPrimaryControlInfo() { return getWhite(); }

    /**
     * Returns the primary control highlight color. This
     * returns the value of {@code getBlack()}.
     *
     * @return the primary control highlight color
     */
    @Override
    public ColorUIResource getPrimaryControlHighlight() { return getBlack(); }

    /**
     * Returns the system text color. This returns the value of
     * {@code getWhite()}.
     *
     * @return the system text color
     */
    @Override
    public ColorUIResource getSystemTextColor() { return getWhite(); }

    /**
     * Returns the control text color. This returns the value of
     * {@code getControlInfo()}.
     *
     * @return the control text color
     */
    @Override
    public ColorUIResource getControlTextColor() { return getControlInfo(); }

    /**
     * Returns the inactive control text color. This returns the value of
     * {@code getControlDisabled()}.
     *
     * @return the inactive control text color
     */
    @Override
    public ColorUIResource getInactiveControlTextColor() { return getControlDisabled(); }

    /**
     * Returns the inactive system text color. This returns the value of
     * {@code getSecondary2()}.
     *
     * @return the inactive system text color
     */
    @Override
    public ColorUIResource getInactiveSystemTextColor() { return getSecondary2(); }

    /**
     * Returns the user text color. This returns the value of
     * {@code getWhite()}.
     *
     * @return the user text color
     */
    @Override
    public ColorUIResource getUserTextColor() { return getWhite(); }

    /**
     * Returns the text highlight color. This returns the value of
     * {@code getPrimary3()}.
     *
     * @return the text highlight color
     */
    @Override
    public ColorUIResource getTextHighlightColor() { return getPrimary3(); }

    /**
     * Returns the highlighted text color. This returns the value of
     * {@code getControlTextColor()}.
     *
     * @return the highlighted text color
     */
    @Override
    public ColorUIResource getHighlightedTextColor() { return getControlTextColor(); }

    /**
     * Returns the window background color. This returns the value of
     * {@code getBlack()}.
     *
     * @return the window background color
     */
    @Override
    public ColorUIResource getWindowBackground() { return getBlack(); }

    /**
     * Returns the window title background color. This returns the value of
     * {@code getPrimary3()}.
     *
     * @return the window title background color
     */
    @Override
    public ColorUIResource getWindowTitleBackground() { return getPrimary3(); }

    /**
     * Returns the window title foreground color. This returns the value of
     * {@code getWhite()}.
     *
     * @return the window title foreground color
     */
    @Override
    public ColorUIResource getWindowTitleForeground() { return getWhite(); }

    /**
     * Returns the window title inactive background color. This
     * returns the value of {@code getSecondary3()}.
     *
     * @return the window title inactive background color
     */
    @Override
    public ColorUIResource getWindowTitleInactiveBackground() { return getSecondary3(); }

    /**
     * Returns the window title inactive foreground color. This
     * returns the value of {@code getWhite()}.
     *
     * @return the window title inactive foreground color
     */
    @Override
    public ColorUIResource getWindowTitleInactiveForeground() { return getWhite(); }

    /**
     * Returns the menu background color. This
     * returns the value of {@code getSecondary3()}.
     *
     * @return the menu background color
     */
    @Override
    public ColorUIResource getMenuBackground() { return getSecondary3(); }

    /**
     * Returns the menu foreground color. This
     * returns the value of {@code getWhite()}.
     *
     * @return the menu foreground color
     */
    @Override
    public ColorUIResource getMenuForeground() { return  getWhite(); }

    /**
     * Returns the menu selected background color. This
     * returns the value of {@code getPrimary2()}.
     *
     * @return the menu selected background color
     */
    @Override
    public ColorUIResource getMenuSelectedBackground() { return getPrimary2(); }

    /**
     * Returns the menu selected foreground color. This
     * returns the value of {@code getWhite()}.
     *
     * @return the menu selected foreground color
     */
    @Override
    public ColorUIResource getMenuSelectedForeground() { return getWhite(); }

    /**
     * Returns the menu disabled foreground color. This
     * returns the value of {@code getSecondary2()}.
     *
     * @return the menu disabled foreground color
     */
    @Override
    public ColorUIResource getMenuDisabledForeground() { return getSecondary2(); }

    /**
     * Returns the separator background color. This
     * returns the value of {@code getBlack()}.
     *
     * @return the separator background color
     */
    @Override
    public ColorUIResource getSeparatorBackground() { return getBlack(); }

    /**
     * Returns the separator foreground color. This
     * returns the value of {@code getPrimary1()}.
     *
     * @return the separator foreground color
     */
    @Override
    public ColorUIResource getSeparatorForeground() { return getPrimary1(); }

    /**
     * Returns the accelerator foreground color. This
     * returns the value of {@code getPrimary1()}.
     *
     * @return the accelerator foreground color
     */
    @Override
    public ColorUIResource getAcceleratorForeground() { return getPrimary1(); }

    /**
     * Returns the accelerator selected foreground color. This
     * returns the value of {@code getWhite()}.
     *
     * @return the accelerator selected foreground color
     */
    @Override
    public ColorUIResource getAcceleratorSelectedForeground() { return getWhite(); }

    /**
     * Returns the name of this theme. This returns {@code "Dark"}.
     *
     * @return the name of this theme.
     */
    public String getName() { return "Dark"; }

    /**
     * Creates and returns an instance of {@code DarkMetalTheme}.
     */
    public DarkMetalTheme() {
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

    /*
    /**
     * Returns the control text font. This returns Dialog, 12pt. If
     * plain fonts have been enabled as described in <a href="#fontStyle">
     * font style</a>, the font style is plain. Otherwise the font style is
     * bold.
     *
     * @return the control text font
     * /
    public FontUIResource getControlTextFont() {
        return getFont(CONTROL_TEXT_FONT);
    }

    /**
     * Returns the system text font. This returns Dialog, 12pt, plain.
     *
     * @return the system text font
     * /
    public FontUIResource getSystemTextFont() {
        return getFont(SYSTEM_TEXT_FONT);
    }

    /**
     * Returns the user text font. This returns Dialog, 12pt, plain.
     *
     * @return the user text font
     * /
    public FontUIResource getUserTextFont() {
        return getFont(USER_TEXT_FONT);
    }

    /**
     * Returns the menu text font. This returns Dialog, 12pt. If
     * plain fonts have been enabled as described in <a href="#fontStyle">
     * font style</a>, the font style is plain. Otherwise the font style is
     * bold.
     *
     * @return the menu text font
     * /
    public FontUIResource getMenuTextFont() {
        return getFont(MENU_TEXT_FONT);
    }

    /**
     * Returns the window title font. This returns Dialog, 12pt, bold.
     *
     * @return the window title font
     * /
    public FontUIResource getWindowTitleFont() {
        return getFont(WINDOW_TITLE_FONT);
    }

    /**
     * Returns the sub-text font. This returns Dialog, 10pt, plain.
     *
     * @return the sub-text font
     * /
    public FontUIResource getSubTextFont() {
        return getFont(SUB_TEXT_FONT);
    }

    private FontUIResource getFont(int key) {
        return fontDelegate.getFont(key);
    }

    void install() {
        if (MetalLookAndFeel.isWindows() &&
                             MetalLookAndFeel.useSystemFonts()) {
            fontDelegate = new WindowsFontDelegate();
        }
        else {
            fontDelegate = new FontDelegate();
        }
    }

    /**
     * Returns true if this is a theme provided by the core platform.
     * /
    boolean isSystemTheme() {
        return (getClass() == DefaultMetalTheme.class);
    }

    /**
     * FontDelegates add an extra level of indirection to obtaining fonts.
     * /
    private static class FontDelegate {
        private static int[] defaultMapping = {
            CONTROL_TEXT_FONT, SYSTEM_TEXT_FONT,
            USER_TEXT_FONT, CONTROL_TEXT_FONT,
            CONTROL_TEXT_FONT, SUB_TEXT_FONT
        };
        FontUIResource fonts[];

        // menu and window are mapped to controlFont
        public FontDelegate() {
            fonts = new FontUIResource[6];
        }

        public FontUIResource getFont(int type) {
            int mappedType = defaultMapping[type];
            if (fonts[type] == null) {
                Font f = getPrivilegedFont(mappedType);

                if (f == null) {
                    f = new Font(getDefaultFontName(type),
                             getDefaultFontStyle(type),
                             getDefaultFontSize(type));
                }
                fonts[type] = new FontUIResource(f);
            }
            return fonts[type];
        }

        /**
         * This is the same as invoking
         * <code>Font.getFont(key)</code>, with the exception
         * that it is wrapped inside a <code>doPrivileged</code> call.
         * /
        protected Font getPrivilegedFont(final int key) {
            return java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Font>() {
                    public Font run() {
                        return Font.getFont(getDefaultPropertyName(key));
                    }
                }
                );
        }
    }

    /**
     * The WindowsFontDelegate uses DesktopProperties to obtain fonts.
     * /
    private static class WindowsFontDelegate extends FontDelegate {
        private MetalFontDesktopProperty[] props;
        private boolean[] checkedPriviledged;

        public WindowsFontDelegate() {
            props = new MetalFontDesktopProperty[6];
            checkedPriviledged = new boolean[6];
        }

        public FontUIResource getFont(int type) {
            if (fonts[type] != null) {
                return fonts[type];
            }
            if (!checkedPriviledged[type]) {
                Font f = getPrivilegedFont(type);

                checkedPriviledged[type] = true;
                if (f != null) {
                    fonts[type] = new FontUIResource(f);
                    return fonts[type];
                }
            }
            if (props[type] == null) {
                props[type] = new MetalFontDesktopProperty(type);
            }
            // While passing null may seem bad, we don't actually use
            // the table and looking it up is rather expensive.
            return (FontUIResource)props[type].createValue(null);
        }
    }
    */
}