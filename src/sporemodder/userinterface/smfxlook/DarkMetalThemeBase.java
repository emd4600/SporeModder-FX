package sporemodder.userinterface.smfxlook;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;

/**
 * {@code MetalTheme} provides the color palette and fonts used by
 * the Java Look and Feel.
 * <p>
 * {@code MetalTheme} is abstract, see {@code DefaultMetalTheme} and
 * {@code OceanTheme} for concrete implementations.
 * <p>
 * {@code MetalLookAndFeel} maintains the current theme that the
 * the {@code ComponentUI} implementations for metal use. Refer to
 * {@link MetalLookAndFeel#setCurrentTheme
 * MetalLookAndFeel.setCurrentTheme(MetalTheme)} for details on changing
 * the current theme.
 * <p>
 * {@code MetalTheme} provides a number of public methods for getting
 * colors. These methods are implemented in terms of a
 * handful of protected abstract methods. A subclass need only override
 * the protected abstract methods ({@code getPrimary1},
 * {@code getPrimary2}, {@code getPrimary3}, {@code getSecondary1},
 * {@code getSecondary2}, and {@code getSecondary3}); although a subclass
 * may override the other public methods for more control over the set of
 * colors that are used.
 * <p>
 * Concrete implementations of {@code MetalTheme} must return {@code non-null}
 * values from all methods. While the behavior of returning {@code null} is
 * not specified, returning {@code null} will result in incorrect behavior.
 * <p>
 * It is strongly recommended that subclasses return completely opaque colors.
 * To do otherwise may result in rendering problems, such as visual garbage.
 *
 * @see DefaultMetalTheme
 * @see OceanTheme
 * @see MetalLookAndFeel#setCurrentTheme
 *
 * @author Steve Wilson
 */
public abstract class DarkMetalThemeBase extends DefaultMetalTheme {

    // Contants identifying the various Fonts that are Theme can support
    static final int CONTROL_TEXT_FONT = 0;
    static final int SYSTEM_TEXT_FONT = 1;
    static final int USER_TEXT_FONT = 2;
    static final int MENU_TEXT_FONT = 3;
    static final int WINDOW_TITLE_FONT = 4;
    static final int SUB_TEXT_FONT = 5;

    static ColorUIResource white = new ColorUIResource( 255, 255, 255 );
    private static ColorUIResource black = new ColorUIResource( 0, 0, 0 );
    
    public DarkMetalThemeBase() {
        super();
    }

    /**
     * Returns the name of this theme.
     *
     * @return the name of this theme
     */
    @Override
    public abstract String getName();

    /**
     * Returns the primary 1 color.
     *
     * @return the primary 1 color
     */
    @Override
    protected abstract ColorUIResource getPrimary1();  // these are blue in Metal Default Theme

    /**
     * Returns the primary 2 color.
     *
     * @return the primary 2 color
     */
    @Override
    protected abstract ColorUIResource getPrimary2();

    /**
     * Returns the primary 3 color.
     *
     * @return the primary 3 color
     */
    @Override
    protected abstract ColorUIResource getPrimary3();

    /**
     * Returns the secondary 1 color.
     *
     * @return the secondary 1 color
     */
    @Override
    protected abstract ColorUIResource getSecondary1();  // these are gray in Metal Default Theme

    /**
     * Returns the secondary 2 color.
     *
     * @return the secondary 2 color
     */
    @Override
    protected abstract ColorUIResource getSecondary2();

    /**
     * Returns the secondary 3 color.
     *
     * @return the secondary 3 color
     */
    @Override
    protected abstract ColorUIResource getSecondary3();

    /**
     * Returns the white color. This returns opaque white
     * ({@code 0xFFFFFFFF}).
     *
     * @return the white color
     */
    @Override
    protected ColorUIResource getWhite() { return white; }

    /**
     * Returns the black color. This returns opaque black
     * ({@code 0xFF000000}).
     *
     * @return the black color
     */
    @Override
    protected ColorUIResource getBlack() { return black; }

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
     * the value of {@code getBlack()}.
     *
     * @return the control info color
     */
    @Override
    public ColorUIResource getControlInfo() { return getBlack(); }

    /**
     * Returns the control highlight color. This returns
     * the value of {@code getWhite()}.
     *
     * @return the control highlight color
     */
    @Override
    public ColorUIResource getControlHighlight() { return getWhite(); }

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
     * returns the value of {@code getBlack()}.
     *
     * @return the primary control info color
     */
    @Override
    public ColorUIResource getPrimaryControlInfo() { return getBlack(); }

    /**
     * Returns the primary control highlight color. This
     * returns the value of {@code getWhite()}.
     *
     * @return the primary control highlight color
     */
    @Override
    public ColorUIResource getPrimaryControlHighlight() { return getWhite(); }

    /**
     * Returns the system text color. This returns the value of
     * {@code getBlack()}.
     *
     * @return the system text color
     */
    @Override
    public ColorUIResource getSystemTextColor() { return getBlack(); }

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
     * {@code getBlack()}.
     *
     * @return the user text color
     */
    @Override
    public ColorUIResource getUserTextColor() { return getBlack(); }

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
     * {@code getWhite()}.
     *
     * @return the window background color
     */
    @Override
    public ColorUIResource getWindowBackground() { return getWhite(); }

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
     * {@code getBlack()}.
     *
     * @return the window title foreground color
     */
    @Override
    public ColorUIResource getWindowTitleForeground() { return getBlack(); }

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
     * returns the value of {@code getBlack()}.
     *
     * @return the window title inactive foreground color
     */
    @Override
    public ColorUIResource getWindowTitleInactiveForeground() { return getBlack(); }

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
     * returns the value of {@code getBlack()}.
     *
     * @return the menu foreground color
     */
    @Override
    public ColorUIResource getMenuForeground() { return  getBlack(); }

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
     * returns the value of {@code getBlack()}.
     *
     * @return the menu selected foreground color
     */
    @Override
    public ColorUIResource getMenuSelectedForeground() { return getBlack(); }

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
     * returns the value of {@code getWhite()}.
     *
     * @return the separator background color
     */
    @Override
    public ColorUIResource getSeparatorBackground() { return getWhite(); }

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
     * returns the value of {@code getBlack()}.
     *
     * @return the accelerator selected foreground color
     */
    @Override
    public ColorUIResource getAcceleratorSelectedForeground() { return getBlack(); }

    /**
     * Adds values specific to this theme to the defaults table. This method
     * is invoked when the look and feel defaults are obtained from
     * {@code MetalLookAndFeel}.
     * <p>
     * This implementation does nothing; it is provided for subclasses
     * that wish to customize the defaults table.
     *
     * @param table the {@code UIDefaults} to add the values to
     *
     * @see MetalLookAndFeel#getDefaults
     */
    @Override
    public void addCustomEntriesToTable(UIDefaults table) {}
}