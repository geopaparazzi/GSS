/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.servlets;

import static org.hortonmachine.gears.utils.style.Utilities.ff;
import static org.hortonmachine.gears.utils.style.Utilities.sf;

import java.awt.Color;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;

public class GpsPointsStyleUtil {

    /**
     * Creates a default {@link Style} for a point.
     * 
     * @return the default style.
     */
    public static Style createDefaultPointStyle() {

        FeatureTypeStyle featureTypeStyle = sf.createFeatureTypeStyle();
        featureTypeStyle.rules().add(createDefaultPointRule());

        Style style = sf.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);

        return style;
    }

    /**
     * Creates a default {@link Rule} for a point.
     * 
     * @return the default rule.
     */
    public static Rule createDefaultPointRule() {
        Graphic graphic = sf.createDefaultGraphic();
        Mark circleMark = sf.getCircleMark();
        circleMark.setFill(sf.createFill(ff.literal("#" + Integer.toHexString(Color.BLACK.getRGB() & 0xffffff))));
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(circleMark);
        graphic.setSize(ff.literal(8));

        PointSymbolizer pointSymbolizer = sf.createPointSymbolizer();
        Rule rule = sf.createRule();
        rule.setName("New rule");
        rule.symbolizers().add(pointSymbolizer);

        pointSymbolizer.setGraphic(graphic);
        return rule;
    }

}
