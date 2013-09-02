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

import com.stratelia.webactiv.util.FileRepositoryManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.silverpeas.util.ImageLoader;

/**
 *
 * @author ehugonnet
 */
public class ImageUtility {

  /**
   *
   * @param inputBuf
   * @param widthParam
   * @return
   */
  public static Size getWidthAndHeight(BufferedImage inputBuf, int widthParam) {

    // calcul de la taille de la sortie
    double inputBufWidth;
    double inputBufHeight;
    double width = widthParam;
    double ratio;
    double height;
    if (inputBuf.getWidth() > inputBuf.getHeight()) {
      inputBufWidth = inputBuf.getWidth();
      inputBufHeight = inputBuf.getHeight();
      width = widthParam;
      ratio = inputBufWidth / width;
      height = inputBufHeight / ratio;
    } else {
      inputBufWidth = inputBuf.getHeight();
      inputBufHeight = inputBuf.getWidth();
      height = widthParam;
      ratio = inputBufWidth / width;
      width = inputBufHeight / ratio;
    }
    return new Size(extractIntValue(width), extractIntValue(height));
  }

  private static int extractIntValue(Double doubleValue) {
    String doubleString = Double.toString(doubleValue);
    return Integer.parseInt(doubleString.substring(0, doubleString.indexOf('.')));
  }

  public static Size getWidthAndHeight(String instanceId, String subDir, String imageName,
    int baseWidth) throws IOException {
    String[] directory = new String[]{subDir};
    File image = new File(FileRepositoryManager.getAbsolutePath(instanceId, directory) + imageName);
    BufferedImage inputBuf = ImageLoader.loadImage(image);
    if (inputBuf == null) {
      return new Size(0, 0);
    }

    return getWidthAndHeight(inputBuf, baseWidth);
  }
}
