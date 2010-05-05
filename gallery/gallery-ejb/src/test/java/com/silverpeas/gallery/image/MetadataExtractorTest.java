/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.image;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.silverpeas.util.PathTestUtil;
import com.silverpeas.gallery.model.MetaData;
import java.io.File;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class MetadataExtractorTest {

  MetadataExtractor extractor;
  File koala = new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "Koala.jpg");
  File sunset = new File(
      PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "Coucher de soleil.jpg");

  @Before
  public void setUp() {
    extractor = new MetadataExtractor();
  }

  @Test
  public void testLoadExtractor() {
    List<IptcProperty> properties = extractor.defineImageIptcProperties();
    assertNotNull(properties);
    assertEquals(24, properties.size());

    List<ExifProperty> exifProperties = extractor.defineImageProperties();
    assertNotNull(exifProperties);
    assertEquals(7, exifProperties.size());
  }

  @Test
  public void testExtractImageIptcMetaData() {
    try {
      List<MetaData> metadata = extractor.extractImageIptcMetaData(koala);
      assertNotNull(metadata);
      assertEquals(0, metadata.size());
      metadata = extractor.extractImageIptcMetaData(sunset);
      assertNotNull(metadata);
      assertEquals(17, metadata.size());


      MetaData meta = metadata.get(0);
      assertEquals("622", meta.getProperty());
      assertEquals("(IPTC) Crédit", meta.getLabel());
      assertEquals("Crédit", meta.getValue());

      meta = metadata.get(1);
      assertEquals("634", meta.getProperty());
      assertEquals("Auteur", meta.getLabel());
      assertEquals("Nom de l'auteur : Tag_writer", meta.getValue());

      meta = metadata.get(2);
      assertEquals("592", meta.getProperty());
      assertEquals("Créateur", meta.getLabel());
      assertEquals("Nom du créateur : Tag_by_line", meta.getValue());



    } catch (Exception ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    }
  }

  @Test
  public void testExtractImageExifMetaData() throws UnsupportedEncodingException,
      JpegProcessingException {
    try {
      List<MetaData> metadata = extractor.extractImageExifMetaData(koala);
      assertNotNull(metadata);
      assertEquals(2, metadata.size());
      MetaData meta = metadata.get(0);
      assertEquals("306", meta.getProperty());
      assertEquals("Date de prise de vue", meta.getLabel());
      assertEquals("2009:03:12 13:48:28", meta.getValue());

      meta = metadata.get(1);
      assertEquals("40093", meta.getProperty());
      assertEquals("(Windows) Auteur", meta.getLabel());

      metadata = extractor.extractImageExifMetaData(sunset);
      assertNotNull(metadata);
      assertEquals(5, metadata.size());
      meta = metadata.get(0);
      assertEquals("40093", meta.getProperty());
      assertEquals("(Windows) Auteur", meta.getLabel());
      assertEquals("L'auteur EXIF", meta.getValue());

      meta = metadata.get(1);
      assertEquals("40092", meta.getProperty());
      assertEquals("(Windows) Commentaires", meta.getLabel());
      assertEquals("et un commentaire EXIF", meta.getValue());

      meta = metadata.get(2);
      assertEquals("40094", meta.getProperty());
      assertEquals("(Windows) mots clef", meta.getLabel());
      assertEquals("des mots clés EXIF", meta.getValue());

      meta = metadata.get(3);
      assertEquals("40095", meta.getProperty());
      assertEquals("(Windows) Sujet", meta.getLabel());
      assertEquals("l'objet EXIF", meta.getValue());

      meta = metadata.get(4);
      assertEquals("40091", meta.getProperty());
      assertEquals("(Windows) Titre", meta.getLabel());
      assertEquals("Le titre EXIF", meta.getValue());

    } catch (Exception ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    }
  }

  @After
  public void cleanUp() {
    // code that will be invoked after this test ends
  }
}
