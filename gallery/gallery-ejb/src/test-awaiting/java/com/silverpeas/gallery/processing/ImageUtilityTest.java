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

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class ImageUtilityTest {

  public ImageUtilityTest() {
  }
  //Size of koala (1024 x 768)
  File koala = getDocumentNamed("/Koala.jpg");

  /**
   * Test of getWidthAndHeight method, of class ImageUtility.
   */
  @Test
  public void testGetWidthAndHeightBufferedImageForWidth() throws Exception {
    BufferedImage inputBuf = ImageIO.read(koala);
    int widthParam = 512;
    Size expResult = new Size(512, 384);
    Size result = ImageUtility.getWidthAndHeight(inputBuf, widthParam);
    assertThat(result, is(expResult));


    widthParam = 256;
    expResult = new Size(256, 192);
    result = ImageUtility.getWidthAndHeight(inputBuf, widthParam);
    assertThat(result, is(expResult));
  }

  /**
   * Test of getWidthAndHeight method, of class ImageUtility.
   */
  @Test
  public void testGetWidthAndHeight_4args() throws Exception {
    String instanceId = "";
    String subDir = koala.getParentFile().getAbsolutePath();
    String imageName = koala.getName();
    int baseWidth = 512;
    Size expResult = new Size(512, 384);
    Size result = ImageUtility.getWidthAndHeight(instanceId, subDir, imageName, baseWidth);
    assertThat(result, is(expResult));

    baseWidth = 256;
    expResult = new Size(256, 192);
    result = ImageUtility.getWidthAndHeight(instanceId, subDir, imageName, baseWidth);
    assertThat(result, is(expResult));
  }

  private static File getDocumentNamed(final String name) {
    final URL documentLocation = ImageUtilityTest.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }
}
