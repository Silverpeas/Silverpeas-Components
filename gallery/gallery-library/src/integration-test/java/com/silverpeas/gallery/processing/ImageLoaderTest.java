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
import com.silverpeas.gallery.image.MetadataExtractorTest;
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

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ImageLoaderTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(ImageLoaderTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addAsResource("maven.properties");
        }).build();
  }

  public ImageLoaderTest() {
  }

  //Size of koala (1024 x 768)
  private File koala;
  //Size of koala (1024 x 768)
  private File cmyk;

  private String targetDir;

  @Before
  public void setUp() {
    koala = getDocumentNamed("/Koala.jpg");
    cmyk = getDocumentNamed("/imageJpgCmjn.jpg");
    targetDir = mavenTargetDirectoryRule.getResourceTestDirFile().getAbsolutePath();
  }

  /**
   * Test of loadImage method, of class ImageLoader.
   */
  @Test
  public void testLoadImage() throws Exception {
    File dir = new File(targetDir + "result" + File.separator);
    dir.mkdirs();
    BufferedImage result = ImageLoader.loadImage(koala);
    ImageIO.write(result, "JPEG",
        new File(targetDir + "result" + File.separator + "Koala_loaded.jpg"));
    result = ImageLoader.loadImage(cmyk);
    ImageIO.write(result, "JPEG",
        new File(targetDir + "result" + File.separator + "imageJpgCmjn_loaded.jpg"));
  }

  private File getDocumentNamed(final String name) {
    try {
      return new File(mavenTargetDirectoryRule.getResourceTestDirFile() + name);
    } catch (Exception e) {
      return null;
    }
  }
}
