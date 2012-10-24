/*
 *  Copyright (C) 2000 - 2012 Silverpeas
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

import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 *
 * @author ehugonnet
 */
public class ImageResizer {

  final static ResourceLocator gallerySettings = new ResourceLocator(
    "com.silverpeas.gallery.settings.gallerySettings", "");
  private BufferedImage imageSource;
  private int maxSize;
  private int width = 60;
  private int height = 60;

  public ImageResizer(BufferedImage imageSource, int maxSize) {
    this.imageSource = imageSource;
    this.maxSize = maxSize;
  }

  public void resizeImageWithWatermark(OutputStream outputStream, String nameWatermark, int sizeWatermark)
    throws IOException {
    BufferedImage scaledImage = loadImage();
    Font watermarkFont = new Font("Arial", Font.BOLD, sizeWatermark);
    if (scaledImage != null) {
      Watermarker watermarker = new Watermarker(width, height);
      watermarker.addWatermark(scaledImage, watermarkFont, nameWatermark, sizeWatermark);
    }
    ImageIO.write(scaledImage, "JPEG", outputStream);
  }

  public void resizeImage(OutputStream outputStream) throws IOException {
    BufferedImage scaledImage = loadImage();
    if (scaledImage != null) {
      ImageIO.write(scaledImage, "JPEG", outputStream);
    }
  }

  protected BufferedImage loadImage() throws IOException {
    if (imageSource == null) {
      return null;
    }
    Size size = ImageUtility.getWidthAndHeight(imageSource, maxSize);
    width = size.getWidth();
    height = size.getHeight();
    boolean higherQuality = gallerySettings.getBoolean("UseHigherQuality", true);
    return scaleImage(imageSource, width, height, VALUE_INTERPOLATION_BICUBIC, higherQuality);
  }

  public BufferedImage scaleImage(BufferedImage img,
    int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
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
}
