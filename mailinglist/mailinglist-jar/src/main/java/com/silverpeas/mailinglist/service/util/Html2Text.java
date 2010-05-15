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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.mailinglist.service.util;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * HTML parser, used to extract text form the HTML.
 * @author Emmanuel Hugonnet
 */
public class Html2Text extends HTMLEditorKit.ParserCallback implements HtmlCleaner {
  private StringBuffer texte;
  private boolean register = false;
  private int inScript = 0;
  private boolean hasError = false;
  private boolean isFormatTag = false;
  private int maxSize = 0;

  /**
   * Constructor.
   * @param maxSize the maximum size of the extracted text.
   */
  public Html2Text() {
  }

  public Html2Text(int maxSize) {
    this.maxSize = maxSize;
  }

  public void setSummarySize(int maxSize) {
    this.maxSize = maxSize;
  }

  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    hasError = false;
    if (!register) {
      register = HTML.Tag.BODY.equals(t);
    }
    if (HTML.Tag.SCRIPT.equals(t)) {
      register = false;
      inScript++;
    }
    if (HTML.Tag.META.equals(t) || HTML.Tag.OPTION.equals(t)
        || HTML.Tag.LINK.equals(t)) {
      register = false;
    }
    if (HTML.Tag.I.equals(t) || HTML.Tag.B.equals(t) || HTML.Tag.BIG.equals(t)
        || HTML.Tag.CENTER.equals(t) || HTML.Tag.FONT.equals(t)
        || HTML.Tag.SMALL.equals(t) || HTML.Tag.SPAN.equals(t)
        || HTML.Tag.U.equals(t) || HTML.Tag.H1.equals(t)
        || HTML.Tag.H2.equals(t) || HTML.Tag.H3.equals(t)
        || HTML.Tag.H4.equals(t) || HTML.Tag.H5.equals(t)
        || HTML.Tag.H6.equals(t)) {
      isFormatTag = true;
    }
  }

  public void handleError(String errorMsg, int pos) {
    hasError = !errorMsg.startsWith("invalid.tagatt");
  }

  public void handleEndTag(HTML.Tag t, int pos) {
    if (HTML.Tag.SCRIPT.equals(t)) {
      register = true;
      inScript--;
      if (inScript < 0) {
        inScript = 0;
      }
    }
    if (HTML.Tag.META.equals(t) || HTML.Tag.OPTION.equals(t)
        || HTML.Tag.LINK.equals(t)) {
      register = true;
    }
    if (HTML.Tag.I.equals(t) || HTML.Tag.B.equals(t) || HTML.Tag.BIG.equals(t)
        || HTML.Tag.CENTER.equals(t) || HTML.Tag.FONT.equals(t)
        || HTML.Tag.SMALL.equals(t) || HTML.Tag.SPAN.equals(t)
        || HTML.Tag.U.equals(t) || HTML.Tag.H1.equals(t)
        || HTML.Tag.H2.equals(t) || HTML.Tag.H3.equals(t)
        || HTML.Tag.H4.equals(t) || HTML.Tag.H5.equals(t)
        || HTML.Tag.H6.equals(t)) {
      isFormatTag = false;
    }
  }

  public void parse(Reader in) throws IOException {
    texte = new StringBuffer();
    ParserDelegator delegator = new ParserDelegator();
    delegator.parse(in, this, true);
  }

  public void handleText(char[] text, int pos) {
    if (register && inScript <= 0 && !hasError && texte.length() <= maxSize) {
      for (int i = 0; i < text.length; i++) {
        if (Character.isLetterOrDigit(text[i])) {
          texte.append(text[i]);
        } else if (Character.isSpaceChar(text[i])) {
          texte.append(' ');
        } else {
          texte.append(text[i]);
        }
      }
      if (!isFormatTag && !Character.isSpaceChar(text[text.length - 1])) {
        texte.append(' ');
      }
    }
  }

  public void handleEndOfLineString(String eol) {
    if (texte.length() <= maxSize) {
      texte.append(' ');
    }
  }

  public void handleComment(char[] data, int pos) {
  }

  public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
  }

  @Override
  public String getSummary() {
    String buffer = texte.toString();
    buffer = buffer.trim();
    buffer = buffer.replaceAll("<[B,b][R,r]>", " ");
    buffer = buffer.replaceAll("<[B,b][R,r]/>", " ");
    buffer = buffer.replaceAll("\\s[\\s]*", " ");
    if (buffer.length() <= maxSize) {
      return buffer;
    }
    return buffer.substring(0, maxSize);
  }
}
