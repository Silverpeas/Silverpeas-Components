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

package org.silverpeas.components.kmelia.servlets.renderers;

import org.apache.ecs.wml.Img;
import org.owasp.encoder.Encode;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.date.TemporalFormatter;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * Renderer of the last publications published in a given Kmelia instance.
 *
 * @author mmoquillon
 */
public class LastPublicationsRenderer implements Renderer {

  @Override
  public void render(Writer writer, RenderingContext ctx) throws IOException {
    writer.write("<div class=\"tableBoard\" id=\"latestPublications\">");

    var kmeliaScc = ctx.getSessionController();
    var resources = ctx.getResources();
    List<KmeliaPublication> pubs = kmeliaScc.getLatestPublications();
    boolean displayLinks = URLUtil.displayUniversalLinks();
    String language = kmeliaScc.getCurrentLanguage();

    Img img = new Img(resources.getIcon("kmelia.publication"));
    writer.write("<div id=\"pubsHeader\">" + img);
    writer.write("<b>" + kmeliaScc.getString("PublicationsLast") +
        "</b></div>");

    if (!pubs.isEmpty()) {
      writer.write("<ul class=\"list-publication-home\">");
      String linkIcon = resources.getIcon("kmelia.link");
      for (KmeliaPublication kmeliaPub : pubs) {
        PublicationDetail pub = kmeliaPub.getDetail();
        String shortcut;
        if (pub.isAlias()) {
          shortcut = " (" + resources.getString("kmelia.Shortcut") + ")";
        } else {
          shortcut = "";
        }

        final String liClass = Optional.of(" class=\"new-contribution\"")
            .filter(s -> pub.isNew())
            .orElse(EMPTY);
        writer.write("<li" + liClass + ">");
        writer.write("<div class=\"publication-name line1\"><a class=\"sp-permalink\"" +
            " href=\"" + pub.getPermalink() + "\">" + Encode.forHtml(pub.getName(language)) +
            "</a>" + shortcut +
            "</div>");

        if (kmeliaScc.showUserNameInList()) {
          writer.write("<span class=\"publication-user\">");
          writer.write(ctx.getLastAuthor(kmeliaPub));
          writer.write("</span>");
        }
        writer.write("<span class=\"publication-date\">" + TemporalFormatter.toLocalizedDate(pub.getVisibility().getPeriod().getStartDate(), kmeliaScc.getZoneId(), kmeliaScc.getLanguage()) +
            "</span>");
        if (displayLinks) {
          String link = URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pub.getPK().getId());
          writer.write("<a class=\"sp-permalink publication-hyperlink\" href=\"" + link +
              "\"><img src=\"" + linkIcon +
              "\"  alt=\"" +
              resources.getString("kmelia.CopyPublicationLink") + "\" title=\"" +
              resources.getString("kmelia.CopyPublicationLink") + "\" /></a>");
        }
        writer.write("<p class=\"publication-description\">" + WebEncodeHelper.convertBlanksForHtml(Encode.forHtml(pub.
            getDescription(language))));
        writer.write("</p>");
        writer.write("</li>");
      }

      writer.write("</ul>");
    }

    writer.write("</div>");
  }
}
  