
/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.image;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.gallery.GalleryWarBuilder;
import org.silverpeas.components.gallery.media.AbstractMediaMetadataExtractor;
import org.silverpeas.components.gallery.media.DrewMediaMetadataExtractor;
import org.silverpeas.components.gallery.media.ExifProperty;
import org.silverpeas.components.gallery.media.IptcProperty;
import org.silverpeas.components.gallery.media.MediaMetadataExtractor;
import org.silverpeas.components.gallery.model.MetaData;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.kernel.util.StringUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class MetadataExtractorIT {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(MetadataExtractorIT.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(MediaMetadataExtractor.class, DrewMediaMetadataExtractor.class,
              AbstractMediaMetadataExtractor.class);
          warBuilder.addAsResource(
              "org/silverpeas/gallery/settings/metadataSettings_gallery52.properties");
          warBuilder.addAsResource("org/silverpeas/gallery/settings/metadataSettings.properties");
          warBuilder.addAsResource("org/silverpeas/gallery/multilang/metadataBundle.properties");
        }).build();
  }

  private MediaMetadataExtractor extractor;
  private File koala;
  private File sunset;
  private File dauphins;
  private File chefsUtf8;
  private File pingouin;

  @Before
  public void setUp() {
    extractor = new DrewMediaMetadataExtractor("gallery52");
    koala = getDocumentNamed("/Koala.jpg");
    sunset = getDocumentNamed("/Coucher de soleil.jpg");
    dauphins = getDocumentNamed("/Dauphins-100.jpg");
    chefsUtf8 = getDocumentNamed("/31605rc_utf-8.jpg");
    pingouin = getDocumentNamed("/pingouin.jpg");
  }

  @Test
  public void testLoadExtractor() {
    List<IptcProperty> properties = extractor.defineImageIptcProperties(StringUtil.splitString(
        "IPTC_8,IPTC_9,IPTC_10,IPTC_11,IPTC_12,IPTC_13,IPTC_14,IPTC_15,IPTC_16,IPTC_17,IPTC_18," +
            "IPTC_19,IPTC_20,IPTC_21,IPTC_22,IPTC_23,IPTC_24,IPTC_25,IPTC_26,IPTC_27,IPTC_28," +
            "IPTC_29,IPTC_30,IPTC_31", ','));
    assertNotNull(properties);
    assertEquals(24, properties.size());

    List<ExifProperty> exifProperties = extractor.defineImageProperties(StringUtil
        .splitString("METADATA_1,METADATA_2,METADATA_3,METADATA_4,METADATA_5,METADATA_6,METADATA_7",
            ','));
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
    assertEquals(19, metadata.size());

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
    assertThat(meta.getValue(),
        is("Auberge des Dauphins / Architecture / Vue exterieure / Saou / Foret de Saou /"));
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

  }

  @Test
  public void testExtractImageWithNoMetadata() throws Exception {
    List<MetaData> metadata = extractor.extractImageExifMetaData(pingouin);
    assertNotNull(metadata);
    assertEquals(0, metadata.size());
    metadata = extractor.extractImageIptcMetaData(pingouin);
    assertNotNull(metadata);
    assertEquals(0, metadata.size());
  }

  /**
   * For <a href="https://www.silverpeas.org/redmine/issues/3021">Bug #3021</a>.
   */
  @Test
  public void testExtractImageIptcMetaDataUTF8Encoded() throws Exception {
    List<MetaData> metadata = extractor.extractImageIptcMetaData(chefsUtf8);
    assertNotNull(metadata);
    assertEquals(8, metadata.size());
    MetaData meta = metadata.get(0);
    assertThat(meta.getProperty(), is("622"));
    assertThat(meta.getLabel(), is("(IPTC) Crédit"));
    assertThat(meta.getValue(), is("Francis Rey"));
    meta = metadata.get(1);
    assertThat(meta.getProperty(), is("634"));
    assertThat(meta.getLabel(), is("Auteur"));
    assertThat(meta.getValue(), is("Département de la Drôme"));

    meta = metadata.get(2);
    assertThat(meta.getProperty(), is("592"));
    assertThat(meta.getLabel(), is("Créateur"));
    assertThat(meta.getValue(), is("Francis Rey"));
    meta = metadata.get(3);
    assertThat(meta.getProperty(), is("632"));
    assertThat(meta.getLabel(), is("Légende"));
    assertThat(meta.getValue(),
        is("Salon de l'Agriculture . Durant le salon les cuisiniers Drômois " +
            "ont réalisés en direct et fait déguster des plats dont les recettes étaient " +
            "distribuées " +
            "aux visiteurs"));
    meta = metadata.get(4);
    assertThat(meta.getProperty(), is("602"));
    assertThat(meta.getLabel(), is("Ville"));
    assertThat(meta.getValue(), is("Paris"));
    meta = metadata.get(5);
    assertThat(meta.getProperty(), is("628"));
    assertThat(meta.getLabel(), is("Copyright"));
    assertThat(meta.getValue(), is("©Francis Rey (2012)"));
    meta = metadata.get(6);
    assertThat(meta.getProperty(), is("567"));
    assertThat(meta.getLabel(), is("Date de création"));
    LocalDate date = LocalDate.of(2012, 2, 28);
    assertThat(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
        is(meta.getDateValue()));
    meta = metadata.get(7);
    assertThat(meta.getProperty(), is("572"));
    assertThat(meta.getLabel(), is("572"));
    assertThat(meta.getValue(), is("113641"));
  }

  private File getDocumentNamed(final String name) {
    try {
      return new File(mavenTargetDirectoryRule.getResourceTestDirFile() + name);
    } catch (Exception e) {
      return null;
    }
  }
}
