/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.processing;

import com.silverpeas.gallery.GalleryWarBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.util.ImageLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ImageResizerTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(ImageResizerTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "com.silverpeas.gallery.processing");
          warBuilder.addAsResource("org/silverpeas/gallery/settings/gallerySettings.properties");
          warBuilder.addAsResource("maven.properties");
        }).build();
  }

  public ImageResizerTest() {
  }

  //Size of koala (1024 x 768)
  private File jpegCmyk;
  //Size of koala (1024 x 768)
  private File koala;

  @Before
  public void setUp() {
    jpegCmyk = getDocumentNamed("/imageJpgCmjn.jpg");
    koala = getDocumentNamed("/Koala.jpg");
  }

  /**
   * Test of resizeImage method, of class ImageResizer.
   */
  @Test
  public void testResizeImage() throws Exception {
    File outputFile = new File(koala.getParentFile(), "resizedKoala.jpg");
    ImageResizer instance = new ImageResizer(ImageLoader.loadImage(koala), 512);
    try (OutputStream os = new FileOutputStream(outputFile)) {
      instance.resizeImage(os);
    }
    assertThat(outputFile.exists(), is(true));
    BufferedImage inputBuf = ImageIO.read(outputFile);
    assertThat(inputBuf.getHeight(), is(384));
    assertThat(inputBuf.getWidth(), is(512));
  }

  @Test
  public void testResizeCMYKImage() throws Exception {
    File outputFile = new File(jpegCmyk.getParentFile(), "resizedImageJpgCmjn.jpg");
    ImageResizer instance = new ImageResizer(ImageLoader.loadImage(jpegCmyk), 512);
    try (OutputStream os = new FileOutputStream(outputFile)) {
      instance.resizeImage(os);
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

  private File getDocumentNamed(final String name) {
    try {
      return new File(mavenTargetDirectoryRule.getResourceTestDirFile() + name);
    } catch (Exception e) {
      return null;
    }
  }
}
