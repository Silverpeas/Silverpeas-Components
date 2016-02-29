/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
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
package com.silverpeas.mailinglist.service.util;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.lang3.CharEncoding;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestHtml2Text {

  private HtmlCleaner parser;

  @Before
  public void prepareParser() {
    parser = new Html2Text(200);
  }

  @Test
  public void testParse() throws Exception {
    String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"
        + "<html><head><meta content=\"text/html;charset=UTF-8\" "
        + "http-equiv=\"Content-Type\"><title></title></head><body "
        + "bgcolor=\"#ffffff\" text=\"#000000\">Hello World <i>Salut les "
        + "copains </i></body></html>";
    Reader reader = new StringReader(html);
    parser.parse(reader);
    String summary = parser.getSummary();
    assertThat(summary, is(notNullValue()));
    assertThat(summary, is("Hello World Salut les copains"));
  }

  @Test
  public void testParseBigContent() throws Exception {
    Reader reader = new InputStreamReader(TestHtml2Text.class.getResourceAsStream("lemonde.html"),
        CharEncoding.UTF_8);
    parser.parse(reader);
    String summary = parser.getSummary();
    assertThat(summary, is(notNullValue()));
    if ("Oracle Corporation".equals(System.getProperty("java.vendor"))  ||
        ("Sun Microsystems Inc.".equals(System.getProperty("java.vendor")) && "1.6.0_30".compareTo(
        System.getProperty("java.version")) < 0)) {
      assertThat(summary, is("Politique Recherchez depuis sur Le Monde.fr A la Une Le Desk Vidéos "
          + "International *Elections américaines Europe Politique *Municipales & Cantonales 2008 "
          + "Société Carnet Economie Médias Météo Rendez-vou"));
    } else {
      assertThat(summary, is("<!------ OAS SETUP end ------> Oa name=\"top\"> Politique < "
          + "< Recherchez depuis sur Le Monde.fr < <!-- info_sq_1_zone --> "
          + "A la Une Le Desk Vidéos International *Elections américaines Europe " + "Politique *M"));
    }
  }

  @Test
  public void testParseInraContent() throws Exception {
    Reader reader = new InputStreamReader(TestHtml2Text.class.getResourceAsStream("mailInra.html"),
        CharEncoding.UTF_8);
    parser.parse(reader);
    String summary = parser.getSummary();
    assertThat(summary, is(notNullValue()));
    assertThat(summary, is("Bonjour, Lors de la présention des nouveaux outils effectuée "
        + "le 6 avril, il a été émis l'idée par un DU de créer dans l'espace GU "
        + "TOULOUSE de SILVERPEAS un espace privé par unité facilitant l'organisa"));
  }
}
