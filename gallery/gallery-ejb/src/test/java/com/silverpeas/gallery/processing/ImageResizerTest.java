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

import org.silverpeas.util.ImageLoader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import org.junit.Test;

import static com.silverpeas.util.PathTestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 *
 * @author ehugonnet
 */
public class ImageResizerTest {

  public ImageResizerTest() {
  }

   //Size of koala (1024 x 768)
  File jpegCmyk = new File(TARGET_DIR + "test-classes" + SEPARATOR + "imageJpgCmjn.jpg");
   //Size of koala (1024 x 768)
  File koala = new File(TARGET_DIR + "test-classes" + SEPARATOR + "Koala.jpg");

  /**
   * Test of resizeImage method, of class ImageResizer.
   */
  @Test
  public void testResizeImage() throws Exception {
    File outputFile = new File(TARGET_DIR + "test-classes" + SEPARATOR + "resizedKoala.jpg");
    ImageResizer instance = new ImageResizer(ImageLoader.loadImage(koala), 512);
    OutputStream os = new FileOutputStream(outputFile);
    try {
      instance.resizeImage(os);
    } finally {
      os.close();
    }
    assertThat(outputFile.exists(), is(true));
    BufferedImage inputBuf = ImageIO.read(outputFile);
    assertThat(inputBuf.getHeight(), is(384));
    assertThat(inputBuf.getWidth(), is(512));
  }

  @Test
  public void testResizeCMYKImage() throws Exception {
    File outputFile = new File(TARGET_DIR + "test-classes" + SEPARATOR + "resizedImageJpgCmjn.jpg");
    ImageResizer instance = new ImageResizer(ImageLoader.loadImage(jpegCmyk), 512);
    OutputStream os = new FileOutputStream(outputFile);
    try {
      instance.resizeImage(os);
    } finally {
      os.close();
    }
    assertThat(outputFile.exists(), is(true));
    BufferedImage inputBuf = ImageIO.read(outputFile);
    assertThat(inputBuf.getHeight(), is(512));
    assertThat(inputBuf.getWidth(), is(403));
  }

  /**
   * Test of loadImage method, of class ImageResizer.
   */
  @Test
  public void testLoadImage() throws Exception {
    ImageResizer instance = new ImageResizer(ImageLoader.loadImage(koala), 512);
    BufferedImage result = instance.loadImage();
    assertThat(result, is(notNullValue()));
  }


}
