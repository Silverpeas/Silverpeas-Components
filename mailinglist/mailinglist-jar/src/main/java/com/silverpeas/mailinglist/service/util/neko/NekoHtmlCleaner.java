/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mailinglist.service.util.neko;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLTagBalancer;
import org.cyberneko.html.filters.ElementRemover;

import com.silverpeas.mailinglist.service.util.HtmlCleaner;

public class NekoHtmlCleaner implements HtmlCleaner {
  private StringWriter content;
  private XMLParserConfiguration parser;
  private EntityReplaceWriter writer;
  private int maxSize = 0;

  public NekoHtmlCleaner() {
    ElementRemover remover = new NekoElementRemover();
    remover.removeElement("script");
    remover.removeElement("title");
    remover.removeElement("link");
    remover.removeElement("meta");
    remover.removeElement("select");
    remover.acceptElement("br", null);
    content = new StringWriter();

    writer = new EntityReplaceWriter(content, "UTF-8");
    XMLDocumentFilter[] filters = { new HTMLTagBalancer(), remover, writer };
    parser = new HTMLConfiguration();
    parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
    parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
  }

  @Override
  public String getSummary() {
    String buffer = content.toString();
    buffer = buffer.trim();
    buffer = buffer.replaceAll("<[B,b][R,r]>", " ");
    buffer = buffer.replaceAll("<[B,b][R,r]/>", " ");
    buffer = buffer.replaceAll("\\s[\\s]*", " ");
    if (buffer.length() <= maxSize) {
      return buffer;
    }
    return buffer.substring(0, maxSize);
  }

  @Override
  public void parse(Reader in) throws IOException {
    content = new StringWriter();
    writer.setWriter(content);
    XMLInputSource source = new XMLInputSource("-//W3C//DTD HTML 4.01", null,
        null, in, "UTF-8");
    parser.parse(source);
  }

  @Override
  public void setSummarySize(int size) {
    this.maxSize = size;
  }

}
