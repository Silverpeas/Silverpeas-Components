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
      assertEquals("Crýdit", meta.getValue());
      meta = metadata.get(1);
      assertEquals("634", meta.getProperty());
      assertEquals("Auteur", meta.getLabel());
      assertEquals("Nom de l'auteur : Tag_writer", meta.getValue());

    
      /*
       *
       *
       *
       *
       * IPTC_8_TAG = 622
      IPTC_8_LABEL = gallery.iptcCredit
      IPTC_8_SEARCH = true
      IPTC_8_DATE = false

      IPTC_9_TAG = 634
      IPTC_9_LABEL = gallery.iptcAuthor
      IPTC_9_SEARCH = true
      IPTC_9_DATE = false

      IPTC_10_TAG = 592
      IPTC_10_LABEL = gallery.iptc592
      IPTC_10_SEARCH = true
      IPTC_10_DATE = false

      IPTC_11_TAG = 597
      IPTC_11_LABEL = gallery.iptc597
      IPTC_11_SEARCH = true
      IPTC_11_DATE = false

      IPTC_12_TAG = 632
      IPTC_12_LABEL = gallery.iptc632
      IPTC_12_SEARCH = true
      IPTC_12_DATE = false

      IPTC_13_TAG = 527
      IPTC_13_LABEL = gallery.iptc527
      IPTC_13_SEARCH = true
      IPTC_13_DATE = false

      IPTC_14_TAG = 602
      IPTC_14_LABEL = gallery.iptc602
      IPTC_14_SEARCH = true
      IPTC_14_DATE = false

      IPTC_15_TAG = 628
      IPTC_15_LABEL = gallery.iptc628
      IPTC_15_SEARCH = true
      IPTC_15_DATE = false

      IPTC_16_TAG = 613
      IPTC_16_LABEL = gallery.iptc613
      IPTC_16_SEARCH = true
      IPTC_16_DATE = false

      IPTC_17_TAG = 567
      IPTC_17_LABEL = gallery.iptc567
      IPTC_17_SEARCH = true
      IPTC_17_DATE = true

      IPTC_18_TAG = 617
      IPTC_18_LABEL = gallery.iptc617
      IPTC_18_SEARCH = true
      IPTC_18_DATE = false

      IPTC_19_TAG = 537
      IPTC_19_LABEL = gallery.iptc537
      IPTC_19_SEARCH = true
      IPTC_19_DATE = false

      IPTC_20_TAG = 517
      IPTC_20_LABEL = gallery.iptc517
      IPTC_20_SEARCH = true
      IPTC_20_DATE = false

      IPTC_21_TAG = 615
      IPTC_21_LABEL = gallery.iptc615
      IPTC_21_SEARCH = true
      IPTC_21_DATE = false

      IPTC_22_TAG = 577
      IPTC_22_LABEL = gallery.iptc577
      IPTC_22_SEARCH = true
      IPTC_22_DATE = false

      IPTC_23_TAG = 607
      IPTC_23_LABEL = gallery.iptc607
      IPTC_23_SEARCH = true
      IPTC_23_DATE = false

      IPTC_24_TAG = 512
      IPTC_24_LABEL = gallery.iptc512
      IPTC_24_SEARCH = true
      IPTC_24_DATE = false

      IPTC_25_TAG = 542
      IPTC_25_LABEL = gallery.iptc542
      IPTC_25_SEARCH = true
      IPTC_25_DATE = true

      IPTC_26_TAG = 547
      IPTC_26_LABEL = gallery.iptc547
      IPTC_26_SEARCH = true
      IPTC_26_DATE = false

      IPTC_27_TAG = 627
      IPTC_27_LABEL = gallery.iptc627
      IPTC_27_SEARCH = true
      IPTC_27_DATE = false

      IPTC_28_TAG = 552
      IPTC_28_LABEL = gallery.iptc552
      IPTC_28_SEARCH = true
      IPTC_28_DATE = false

      IPTC_29_TAG = 532
      IPTC_29_LABEL = gallery.iptc532
      IPTC_29_SEARCH = true
      IPTC_29_DATE = false

      IPTC_30_TAG = 572
      IPTC_30_LABEL = gallery.iptc572
      IPTC_30_SEARCH = true
      IPTC_30_DATE = false

      IPTC_31_TAG = 522
      IPTC_31_LABEL = gallery.iptc522
      IPTC_31_SEARCH = true
      IPTC_31_DATE = false

      Instructions spéciales : 	les instructions sp�ciales
      Titre : 	le titre dans les donn�es IPTC
      Ref transmission : 	R�f�rence de la transmission
      Pays : 	Pays
      Mots clef : 	rouge soleil mer
      Date de création : 	01/07/2008 00:00
      Titre du créateur : 	Titre du cr�ateur
      Créateur : 	Nom du cr�ateur : Tag_by_line
      Copyright : 	Copyright (IPTC)
      Source : 	Source
      (IPTC) Crédit : 	Cr�dit
      Objet : 	Nom de l'objet
      Date de sortie : 	31/07/2008 00:00*/
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
