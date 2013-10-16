/*
 *  Copyright (C) 2000 - 2013 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.silverpeas.gallery.processing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author ehugonnet
 */
public class Watermarker {

  private int width = 60;
  private int height = 60;

  public Watermarker() {
  }

  public Watermarker(int width, int height) {
    this.width = width;
    this.height = height;
  }

  private void drawWatermark(Graphics2D g, Font watermarkFont, String watermarkLabel,
    int watermarkSize, Color color) {
    g.setColor(color);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setFont(watermarkFont);
    FontMetrics fontMetrics = g.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(watermarkLabel, g);
    g.drawString(watermarkLabel, (width - (int) rect.getWidth()) - watermarkSize,
      (height - (int) rect.getHeight()) - watermarkSize);
  }

  /**
   * Add a watermark to a buffered image.
   * @param scaledImage the buffered image.
   * @param watermarkFont the font to write the watermark in.
   * @param watermarkLabel the label to be written.
   * @param watermarkSize the size of the watermark.
   */
  public void addWatermark(BufferedImage scaledImage, Font watermarkFont, String watermarkLabel,
    int watermarkSize) {
    Graphics2D g = (Graphics2D) scaledImage.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
    g.setComposite(alpha);
    drawWatermark(g, watermarkFont, watermarkLabel, watermarkSize, Color.BLACK);
    drawWatermark(g, watermarkFont, watermarkLabel, watermarkSize / 2, Color.WHITE);
    g.dispose();
  }

  /**
   * Add a watermark to a buffered image fullfilling a new BufferedImage.
   * @param source the buffered image source.
   * @param source the buffered image target : containing the image and the watermark.
   * @param watermarkFont the font to write the watermark in.
   * @param watermarkLabel the label to be written.
   * @param watermarkSize the size of the watermark.
   */
  public void addWatermark(BufferedImage source, BufferedImage target, Font watermarkFont,
    String watermarkLabel, int watermarkSize) {
    Graphics2D g = (Graphics2D) target.getGraphics();
    g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
    g.setComposite(alpha);
    drawWatermark(g, watermarkFont, watermarkLabel, watermarkSize, Color.BLACK);
    drawWatermark(g, watermarkFont, watermarkLabel, watermarkSize / 2, Color.WHITE);
    g.dispose();
  }
}
