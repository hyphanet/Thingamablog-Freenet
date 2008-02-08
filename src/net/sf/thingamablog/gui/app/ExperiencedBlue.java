package net.sf.thingamablog.gui.app;

/*
 * Copyright (c) 2003 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * This software is the proprietary information of JGoodies Karsten Lentzsch.
 * Use is subject to license terms.
 *
 */

import java.awt.Font;

import com.jgoodies.plaf.plastic.theme.ExperienceBlue;

/**
 * A theme with low saturated blue primary colors and a light brown
 * background; it has been optimized to work with Windows XP default desktop
 * settings.
 * <p>
 * Unlike its superclass, ExperienceBlue, this class uses a font appropriate
 * for Displaying Chinese characters.
 *
 * @author	Karsten Lentzsch
 * @see	javax.swing.plaf.metal.MetalTheme
 * @see     com.jgoodies.plaf.plastic.PlasticTheme
 * @see	Font
 */
public final class ExperiencedBlue extends ExperienceBlue {

    /**
     * Returns the name of this theme.
     * 
     * @see javax.swing.plaf.metal.MetalTheme#getName()
     */
    public String getName() {
        return "Experienced Blue";
    }

    /**
     * Looks up and answers a font appropriate for Chinese.
     * Overrides the superclass' Tahoma default choice.
     * 
     * @see com.jgoodies.plaf.plastic.theme.SkyBluerTahoma#getFont0(int)
     */
    protected Font getFont0(int size) {
        /*
         * Hier kannst du die Schriftart, Schriftstil und -Gr��e angeben.
         * Bei der Gr��e musst du umrechnen von 72dpi auf die aktuelle
         * Aufl�sung - wahrscheinlich 96dpi. Ein 10pt Schrift musst du
         * angeben als 10pt*96dpi/72dpi = 13pt.
         */
        //int defaultSize = LookUtils.isLowRes ? 13 : 16;
        int defaultSize = 11;
        //Font font = UIManager.getFont("Menu.font");
        Font font = new Font("SansSerif", Font.PLAIN, defaultSize);
        return font != null ? font : super.getFont0(size);
    }

}