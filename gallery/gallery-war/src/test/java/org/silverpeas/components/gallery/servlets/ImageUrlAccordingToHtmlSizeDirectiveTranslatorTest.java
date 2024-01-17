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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.servlets;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class ImageUrlAccordingToHtmlSizeDirectiveTranslatorTest {

  private static final String SHORT_BAD_URL = "/silverpeas/GalleryInWysiwyg/dummy";

  private static final String STANDARD_URL =
      "/silverpeas/GalleryInWysiwyg/dummy?ImageId=187&ComponentId=gallery4&UseOriginal=true";

  private static final String WITH_SIZE_URL =
      "/silverpeas/GalleryInWysiwyg/dummy?ImageId=187&ComponentId=gallery4&UseOriginal=true&Size=x200";

  private static final String STANDARD_ESCAPED_URL =
      "/silverpeas/GalleryInWysiwyg/dummy?ImageId=187&amp;ComponentId=gallery4&amp;UseOriginal=false";

  private static final String WITH_SIZE_ESCAPED_URL =
      "/silverpeas/GalleryInWysiwyg/dummy?ImageId=187&amp;ComponentId=gallery4&amp;Size=x200&amp;UseOriginal=false";

  @TestManagedBean
  private GalleryInWysiwygRouter.ImageUrlAccordingToHtmlSizeDirectiveTranslator translator;

  @Test
  void isCompliant() {
    assertThat(translator.isCompliantUrl(null), is(false));
    assertThat(translator.isCompliantUrl(""), is(false));
    assertThat(translator.isCompliantUrl("GalleryInWysiwyg"), is(false));
    assertThat(translator.isCompliantUrl("/GalleryInWysiwyg"), is(false));
    assertThat(translator.isCompliantUrl("GalleryInWysiwyg/"), is(false));
    assertThat(translator.isCompliantUrl("/GalleryInWysiwyg/"), is(true));
  }

  @Test
  void translateUrl() {
    assertThat(translator.translateUrl(SHORT_BAD_URL, "", ""), is(SHORT_BAD_URL));
    assertThat(translator.translateUrl(STANDARD_URL, "", ""), is(STANDARD_URL));
    assertThat(translator.translateUrl(STANDARD_ESCAPED_URL, "", ""), is(STANDARD_ESCAPED_URL));
    assertThat(translator.translateUrl(WITH_SIZE_URL, "", ""), is(STANDARD_URL));
    assertThat(translator.translateUrl(WITH_SIZE_ESCAPED_URL, "", ""), is(STANDARD_ESCAPED_URL));

    assertThat(translator.translateUrl(SHORT_BAD_URL, "100", ""), is(SHORT_BAD_URL + "?Size=100x"));
    assertThat(translator.translateUrl(STANDARD_URL, "100", ""), is(STANDARD_URL + "&Size=100x"));
    assertThat(translator.translateUrl(STANDARD_ESCAPED_URL, "100", ""), is(STANDARD_ESCAPED_URL + "&amp;Size=100x"));
    assertThat(translator.translateUrl(WITH_SIZE_URL, "100", ""), is(STANDARD_URL + "&Size=100x"));
    assertThat(translator.translateUrl(WITH_SIZE_ESCAPED_URL, "100", ""), is(STANDARD_ESCAPED_URL + "&amp;Size=100x"));

    assertThat(translator.translateUrl(SHORT_BAD_URL, "", "150"), is(SHORT_BAD_URL + "?Size=x150"));
    assertThat(translator.translateUrl(STANDARD_URL, "", "150"), is(STANDARD_URL + "&Size=x150"));
    assertThat(translator.translateUrl(STANDARD_ESCAPED_URL, "", "150"), is(STANDARD_ESCAPED_URL + "&amp;Size=x150"));
    assertThat(translator.translateUrl(WITH_SIZE_URL, "", "150"), is(STANDARD_URL + "&Size=x150"));
    assertThat(translator.translateUrl(WITH_SIZE_ESCAPED_URL, "", "150"), is(STANDARD_ESCAPED_URL + "&amp;Size=x150"));

    assertThat(translator.translateUrl(SHORT_BAD_URL, "50", "50"), is(SHORT_BAD_URL + "?Size=50x50"));
    assertThat(translator.translateUrl(STANDARD_URL, "50", "50"), is(STANDARD_URL + "&Size=50x50"));
    assertThat(translator.translateUrl(STANDARD_ESCAPED_URL, "50", "50"), is(STANDARD_ESCAPED_URL + "&amp;Size=50x50"));
    assertThat(translator.translateUrl(WITH_SIZE_URL, "50", "50"), is(STANDARD_URL + "&Size=50x50"));
    assertThat(translator.translateUrl(WITH_SIZE_ESCAPED_URL, "50", "50"), is(STANDARD_ESCAPED_URL + "&amp;Size=50x50"));

    assertThat(translator.translateUrl(SHORT_BAD_URL, "", "200"), is(SHORT_BAD_URL + "?Size=x200"));
    assertThat(translator.translateUrl(STANDARD_URL, "", "200"), is(STANDARD_URL + "&Size=x200"));
    assertThat(translator.translateUrl(STANDARD_ESCAPED_URL, "", "200"), is(STANDARD_ESCAPED_URL + "&amp;Size=x200"));
    assertThat(translator.translateUrl(WITH_SIZE_URL, "", "200"), is(WITH_SIZE_URL));
    assertThat(translator.translateUrl(WITH_SIZE_ESCAPED_URL, "", "200"), is(STANDARD_ESCAPED_URL + "&amp;Size=x200"));
  }
}
