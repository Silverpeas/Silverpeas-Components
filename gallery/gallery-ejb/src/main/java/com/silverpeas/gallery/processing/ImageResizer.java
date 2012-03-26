/*
 *  Copyright (C) 2000 - 2011 Silverpeas
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
 *  "http://www.silverpeas.com/legal/licensing"
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

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MimeTypes;
import com.stratelia.webactiv.util.ResourceLocator;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;


import static java.awt.RenderingHints.*;

/**
 *
 * @author ehugonnet
 */
public class ImageResizer {

  final static ResourceLocator gallerySettings = new ResourceLocator(
    "com.silverpeas.gallery.settings.gallerySettings", "");
  private File imageSource;
  private int maxSize;
  private int width = 60;
  private int height = 60;

  public ImageResizer(File imageSource, int maxSize) {
    this.imageSource = imageSource;
    this.maxSize = maxSize;
  }

  public void resizeImageWithWatermark(String outputFile, String nameWatermark, int sizeWatermark)
    throws IOException {
    BufferedImage scaledImage = loadImage();
    Font watermarkFont = new Font("Arial", Font.BOLD, sizeWatermark);
    if (scaledImage != null) {
      Graphics2D g = (Graphics2D) scaledImage.getGraphics();
      g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
      AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
      g.setComposite(alpha);
      drawWatermark(g, watermarkFont, nameWatermark, sizeWatermark / 2, Color.BLACK);
      drawWatermark(g, watermarkFont, nameWatermark, sizeWatermark / 2, Color.WHITE);
      g.dispose();
    }
    // Ecriture du buffer sortie dans le fichier "outputFile" sur disque
    ImageIO.write(scaledImage, "JPEG", new File(outputFile));
  }

  public void resizeImage(String outputFile) throws IOException {
    BufferedImage scaledImage = loadImage();
    if (scaledImage != null) {
      ImageIO.write(scaledImage, "JPEG", new File(outputFile));
    }
  }

  protected BufferedImage loadImage() throws IOException {
    // Create buffer and fill it in with the initial image
    
    BufferedImage inputBuf = ImageLoader.loadImage(imageSource);
    if (inputBuf == null) {
      return null;
    }
    Size size = ImageUtility.getWidthAndHeight(inputBuf, maxSize);
    width = size.getWidth();
    height = size.getHeight();
    boolean higherQuality = gallerySettings.getBoolean("UseHigherQuality", true);
    return scaleImage(inputBuf, width, height, VALUE_INTERPOLATION_BICUBIC, higherQuality);
  }

  public BufferedImage scaleImage(BufferedImage img,
    int targetWidth, int targetHeight, Object hint, boolean higherQuality) {

    // Never try to get a 0-sized picture so that constructor of BufferedImage
    // will not return an IllegalArgumentException
    if (targetWidth < 1) {
      targetWidth = 1;
    }
    if (targetHeight < 1) {
      targetHeight = 1;
    }

    int type = BufferedImage.TYPE_INT_RGB;
    BufferedImage ret = img;
    int w, h;
    if (higherQuality) {
      // Use multi-step technique: start with original size, then
      // scale down in multiple passes with drawImage()
      // until the target size is reached
      w = img.getWidth();
      h = img.getHeight();
    } else {
      // Use one-step technique: scale directly from original
      // size to target size with a single drawImage() call
      w = targetWidth;
      h = targetHeight;
    }

    do {
      if (higherQuality && w > targetWidth) {
        w /= 2;
        if (w < targetWidth) {
          w = targetWidth;
        }
      }

      if (higherQuality && h > targetHeight) {
        h /= 2;
        if (h < targetHeight) {
          h = targetHeight;
        }
      }

      BufferedImage tmp = new BufferedImage(w, h, type);
      Graphics2D g2 = tmp.createGraphics();
      g2.setRenderingHint(KEY_INTERPOLATION, hint);
      g2.drawImage(ret, 0, 0, w, h, null);
      g2.dispose();

      ret = tmp;
    } while (w != targetWidth || h != targetHeight);

    return ret;
  }

  private void drawWatermark(Graphics2D g, Font watermarkFont, String nameWatermark,
    int sizeWatermark, Color color) {
    g.setColor(color);
    g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);      
    g.setFont(watermarkFont);
    FontMetrics fontMetrics = g.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(nameWatermark, g);
    g.drawString(nameWatermark, (width - (int) rect.getWidth())
      - sizeWatermark, (height - (int) rect.getHeight())
      - sizeWatermark);
  }
}
