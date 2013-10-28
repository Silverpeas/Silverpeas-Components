/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.kmelia.export;

import java.util.Iterator;
import java.util.List;

import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Footer;
import org.odftoolkit.simple.text.Header;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Section;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * It is a translator of text containing in some parts of an ODT documents.
 * It is instanciated for a given localized resource bundle from which it will fetch the translated 
 * text.
 * 
 * Currently it translates all the header, footer, paragraphs and tables in an ODT document.
 * 
 * Instances of this class are mainly used to translate predefined text in templates in the ODT
 * format. The text to translate is refered in the document as a key in the backed resource bundle.
 * If the key exists, it is replaced in the ODT document by its associated translation.
 */
public class ODTDocumentTextTranslator {

  private final ResourceLocator bundle;

  /**
   * Creates a translator of ODT texts with the specified localized resource bundle.
   * @param bundle the bundle to use for translating texts.
   * @return an ODTDocumentTextTranslator instance.
   */
  public static ODTDocumentTextTranslator aTranslatorWith(final ResourceLocator bundle) {
    return new ODTDocumentTextTranslator(bundle);
  }

  private ODTDocumentTextTranslator(final ResourceLocator bundle) {
    this.bundle = bundle;
  }

  /**
   * Translates the specified ODT document.
   * 
   * Each text to translate in the document should be actually a key in the resource bundle to the
   * translated text to use; the key in the document is then replaced by the associated translated
   * text. If the text is not a key in the resource bundle, then it isn't translated and it is kept
   * as such in the document.
   * @param document the document to translate.
   */
  public void translate(final TextDocument document) {
    translateHeader(document.getHeader());
    translateFooter(document.getFooter());
    translateParagraphs(document.getParagraphIterator());
    translateTables(document.getTableList().iterator());
    translateSections(document.getSectionIterator());
  }

  private void translateTables(final Iterator<Table> tables) {
    while (tables.hasNext()) {
      Table table = tables.next();
      List<Column> columns = table.getColumnList();
      for (Column column : columns) {
        for (int i = 0; i < column.getCellCount(); i++) {
          String translatedTextKey = column.getCellByIndex(i).getStringValue();
          if (!translatedTextKey.isEmpty()) {
            String translatedText = translateText(translatedTextKey);
            column.getCellByIndex(i).setStringValue(translatedText);
          }
        }
      }
    }
  }

  /**
   * Specific processes for the header and the footer of an ODT document.
   * The header and the footer text can contain fields and it is important not erasing them by
   * rewritting the text with translated words. So, the text of headers and footers are taken as
   * an XML element and their childs are treated differently according to their type (text to
   * translate is the content of the XML elements text:span).
   * @param tables the tables that made a header or a footer.
   */
  private void translateFooterHeaderTables(final Iterator<Table> tables) {
    while (tables.hasNext()) {
      Table table = tables.next();
      TableTableElement elt = table.getOdfElement();
      NodeList spanNodes = elt.getElementsByTagName("text:span");
      for (int i = 0; i < spanNodes.getLength(); i++) {
        Node text = spanNodes.item(i);
        String content = text.getTextContent();
        if (content !=null && !content.isEmpty()) {
          text.setTextContent(translateText(text.getTextContent()));
        }
      }
    }
  }

  private void translateParagraphs(final Iterator<Paragraph> paragraphs) {
    while (paragraphs.hasNext()) {
      Paragraph p = paragraphs.next();
      String translatedTextKey = p.getTextContent();
      String translatedText = translateText(translatedTextKey);
      p.setTextContent(translatedText);
    }
  }

  private void translateSections(final Iterator<Section> sections) {
    while (sections.hasNext()) {
      Section section = sections.next();
      translateParagraphs(section.getParagraphIterator());
    }
  }

  private void translateHeader(final Header header) {
    List<Table> tables = header.getTableList();
    translateFooterHeaderTables(tables.listIterator());
  }

  private void translateFooter(final Footer footer) {
    List<Table> tables = footer.getTableList();
    translateFooterHeaderTables(tables.listIterator());
  }

  private String translateText(String text) {
    StringBuilder translatedText = new StringBuilder();
    String[] words = text.split(" ");
    for (String aWord : words) {
      if (!aWord.isEmpty()) {
        translatedText.append(getBundle().getString(aWord.trim(), aWord));
      }
      translatedText.append(" ");
    }
    return translatedText.toString();
  }

  private ResourceLocator getBundle() {
    return this.bundle;
  }
}
