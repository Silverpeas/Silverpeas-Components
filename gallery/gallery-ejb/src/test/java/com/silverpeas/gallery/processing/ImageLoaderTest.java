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

import static com.silverpeas.util.PathTestUtil.SEPARATOR;
import static com.silverpeas.util.PathTestUtil.TARGET_DIR;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.silverpeas.util.ImageLoader;

/**
 *
 * @author ehugonnet
 */
public class ImageLoaderTest {

  public ImageLoaderTest() {
  }
//Size of koala (1024 x 768)
  File koala = new File(TARGET_DIR + "test-classes" + SEPARATOR + "Koala.jpg");
  //Size of koala (1024 x 768)
  File cmyk = new File(TARGET_DIR + "test-classes" + SEPARATOR + "imageJpgCmjn.jpg");

  /**
   * Test of loadImage method, of class ImageLoader.
   */
  @Test
  public void testLoadImage() throws Exception {
    File dir = new File(TARGET_DIR + "result"+ SEPARATOR);
    dir.mkdirs();
    BufferedImage result = ImageLoader.loadImage(koala);
    ImageIO.write(result, "JPEG", new File(
      TARGET_DIR + "result" + SEPARATOR + "Koala_loaded.jpg"));
    result = ImageLoader.loadImage(cmyk);
    ImageIO.write(result, "JPEG", new File(
      TARGET_DIR + "result" + SEPARATOR + "imageJpgCmjn_loaded.jpg"));
  }
}
