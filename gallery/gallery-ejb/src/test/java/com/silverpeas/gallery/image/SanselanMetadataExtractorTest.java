/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.image;

import com.silverpeas.gallery.model.MetaData;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.silverpeas.util.PathTestUtil.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static com.silverpeas.gallery.image.AbstractImageMetadataExtractor.COMMA_SPLITTER;

/**
 * @author ehugonnet
 */
public class SanselanMetadataExtractorTest {

  ImageMetadataExtractor extractor;
  File koala = new File(TARGET_DIR + "test-classes" + SEPARATOR + "Koala.jpg");
  File sunset = new File(TARGET_DIR + "test-classes" + SEPARATOR + "Sunset.jpg");
  File gmt = new File(TARGET_DIR + "test-classes" + SEPARATOR + "w40_DSC_7481.jpg");
  File dauphins = new File(TARGET_DIR + "test-classes" + SEPARATOR + "Dauphins-100.jpg");
  File chefs = new File(TARGET_DIR + "test-classes" + SEPARATOR + "31605rc_utf-8.jpg");

  @Before
  public void setUp() {
    extractor = new SanselanImageMetadataExtractor("gallery52");
  }

  @Test
  public void testLoadExtractor() {
    List<IptcProperty> properties = extractor.defineImageIptcProperties(COMMA_SPLITTER.split(
      "IPTC_8,IPTC_9,IPTC_10,IPTC_11,IPTC_12,IPTC_13,IPTC_14,IPTC_15,IPTC_16,IPTC_17,IPTC_18,"
      + "IPTC_19,IPTC_20,IPTC_21,IPTC_22,IPTC_23,IPTC_24,IPTC_25,IPTC_26,IPTC_27,IPTC_28,IPTC_29,"
      + "IPTC_30,IPTC_31"));
    assertNotNull(properties);
    assertEquals(24, properties.size());

    List<ExifProperty> exifProperties = extractor.defineImageProperties(COMMA_SPLITTER.split(
      "METADATA_1,METADATA_2,METADATA_3,METADATA_4,METADATA_5,METADATA_6,METADATA_7"));
    assertNotNull(exifProperties);
    assertEquals(7, exifProperties.size());
  }

  @Test
  public void testExtractImageIptcMetaData() throws Exception {
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


  }

  /**
   * For <a href="https://www.silverpeas.org/redmine/issues/2639">Bug #2639</a>.
   */
  @Test
  public void testExtractImageIptcMetaDataWithGMTCreationDate() throws Exception {
    List<MetaData> metadata = extractor.extractImageIptcMetaData(gmt);
    assertNotNull(metadata);
    for (MetaData metadonnee : metadata) {
      System.out.
        println(metadonnee.getProperty() + " - " + metadonnee.getLabel() + ": " + metadonnee.
        getValue());
    }
    assertEquals(10, metadata.size());
    MetaData meta = metadata.get(0);
    assertThat(meta.getProperty(), is("622"));
    assertThat(meta.getLabel(), is("(IPTC) Crédit"));
    assertThat(meta.getValue(), is("Conservation du patrimoine de la Drôme"));

    meta = metadata.get(5);
    assertThat(meta.getProperty(), is("567"));
    assertThat(meta.getLabel(), is("Date de création"));
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.SEPTEMBER);
    calend.set(Calendar.YEAR, 2011);
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    assertThat(calend.getTime(), is(meta.getDateValue()));

    meta = metadata.get(1);
    assertThat(meta.getProperty(), is("592"));
    assertThat(meta.getLabel(), is("Créateur"));
    assertThat(meta.getValue(), is("Georges Emmanuel"));
    meta = metadata.get(9);
    assertThat(meta.getProperty(), is("572"));
    assertThat(meta.getLabel(), is("572"));
    assertThat(meta.getValue(), is("161908+0000"));
    calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 1);
    calend.set(Calendar.MONTH, Calendar.JANUARY);
    calend.set(Calendar.YEAR, 1970);
    calend.set(Calendar.HOUR_OF_DAY, 17);
    calend.set(Calendar.MINUTE, 19);
    calend.set(Calendar.SECOND, 8);
    calend.set(Calendar.MILLISECOND, 0);
    assertThat(meta.getDateValue(), is(calend.getTime()));
  }

  /**
   * For <a href="https://www.silverpeas.org/redmine/issues/2640">Bug #2640</a>.
   */
  @Test
  public void testExtractImageIptcMetaDataWithMultipleKeywords() throws Exception {
    List<MetaData> metadata = extractor.extractImageIptcMetaData(dauphins);
    assertNotNull(metadata);
    assertEquals(9, metadata.size());
    MetaData meta = metadata.get(0);
    assertThat(meta.getProperty(), is("622"));
    assertThat(meta.getLabel(), is("(IPTC) Crédit"));
    assertThat(meta.getValue(), is("Conservation du patrimoine de la Drome"));

    meta = metadata.get(5);
    assertThat(meta.getProperty(), is("567"));
    assertThat(meta.getLabel(), is("Date de création"));
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 19);
    calend.set(Calendar.MONTH, Calendar.MAY);
    calend.set(Calendar.YEAR, 2010);
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    assertThat(calend.getTime(), is(meta.getDateValue()));

    meta = metadata.get(1);
    assertThat(meta.getProperty(), is("592"));
    assertThat(meta.getLabel(), is("Créateur"));
    assertThat(meta.getValue(), is("Aymard Gilles"));

    meta = metadata.get(7);
    assertThat(meta.getProperty(), is("537"));
    assertThat(meta.getLabel(), is("Mots clef"));
    assertThat(meta.getValue(), is(
      "Auberge des Dauphins /Architecture /Vue exterieure /Saou /Foret de Saou /"));
  }

  @Test
  public void testExtractImageExifMetaData() throws Exception {
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
    assertEquals(6, metadata.size());
    meta = metadata.get(0);
    assertEquals("306", meta.getProperty());
    assertEquals("Date de prise de vue", meta.getLabel());
    assertEquals("2010:07:22 09:11:14", meta.getValue());

    meta = metadata.get(1);
    assertEquals("40093", meta.getProperty());
    assertEquals("(Windows) Auteur", meta.getLabel());
    assertEquals("L'auteur EXIF", meta.getValue());

    meta = metadata.get(2);
    assertEquals("40092", meta.getProperty());
    assertEquals("(Windows) Commentaires", meta.getLabel());
    assertArrayEquals("et un commentaire EXIF".getBytes(), meta.getValue().getBytes());
    assertEquals("et un commentaire EXIF", meta.getValue());

    meta = metadata.get(3);
    assertEquals("40094", meta.getProperty());
    assertEquals("(Windows) mots clef", meta.getLabel());
    assertEquals("des mots clés EXIF", meta.getValue());

    meta = metadata.get(5);
    assertEquals("40091", meta.getProperty());
    assertEquals("(Windows) Titre", meta.getLabel());
    assertEquals("Le titre EXIF", meta.getValue());

    meta = metadata.get(4);
    assertEquals("40095", meta.getProperty());
    assertEquals("(Windows) Sujet", meta.getLabel());
    assertEquals("l'objet EXIT", meta.getValue());
  }
  
  /**
   * For <a href="https://www.silverpeas.org/redmine/issues/3021">Bug #3021</a>.
   */
  @Test
  @Ignore
  public void testExtractImageIptcMetaDataUTF8Encoded() throws Exception {
    List<MetaData> metadata = extractor.extractImageIptcMetaData(chefs);
    assertNotNull(metadata);
    assertEquals(8, metadata.size());
    MetaData meta = metadata.get(0);
    assertThat(meta.getProperty(), is("622"));
    assertThat(meta.getLabel(), is("(IPTC) Crédit"));
    assertThat(meta.getValue(), is("Conservation du patrimoine de la Drome"));
    
     assertThat(meta.getProperty(), is("634"));
    assertThat(meta.getLabel(), is("(IPTC) Crédit"));
    assertThat(meta.getValue(), is("Conservation du patrimoine de la Drome"));

    meta = metadata.get(5);
    assertThat(meta.getProperty(), is("567"));
    assertThat(meta.getLabel(), is("Date de création"));
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 19);
    calend.set(Calendar.MONTH, Calendar.MAY);
    calend.set(Calendar.YEAR, 2010);
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    assertThat(calend.getTime(), is(meta.getDateValue()));

    meta = metadata.get(1);
    assertThat(meta.getProperty(), is("592"));
    assertThat(meta.getLabel(), is("Créateur"));
    assertThat(meta.getValue(), is("Aymard Gilles"));

    meta = metadata.get(7);
    assertThat(meta.getProperty(), is("537"));
    assertThat(meta.getLabel(), is("Mots clef"));
    assertThat(meta.getValue(), is(
      "Auberge des Dauphins /Architecture /Vue exterieure /Saou /Foret de Saou /"));
  }
}
