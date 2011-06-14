/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.stratelia.webactiv.util.ResourceLocator;
import java.util.Iterator;
import java.util.List;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Section;

/**
 * It is a translator of text containing in some parts of an ODT documents.
 * It is instanciated for a given localized resource bundle from which it will fetch the translated 
 * text.
 * 
 * Currently it translates all the paragraphs and tables in an ODT document.
 * 
 * Instances of this class are mainly used to translate predefined text in templates in the ODT
 * format.
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
   * It the keys for translated text are not defined in the backed resource bundle, then the 
   * translation doesn't occur for this key.
   * @param document the document to translate.
   */
  public void translate(final TextDocument document) {
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
            String translatedText = getBundle().getString(translatedTextKey, translatedTextKey);
            column.getCellByIndex(i).setStringValue(translatedText);
          }
        }
      }
    }
  }

  private void translateParagraphs(final Iterator<Paragraph> paragraphs) {
    while (paragraphs.hasNext()) {
      Paragraph p = paragraphs.next();
      String translatedTextKey = p.getTextContent();
      String translatedText = getBundle().getString(translatedTextKey, translatedTextKey);
      p.setTextContent(translatedText);
    }
  }
  
  private void translateSections(final Iterator<Section> sections) {
    while (sections.hasNext()) {
      Section section = sections.next();
      translateParagraphs(section.getParagraphIterator());
    }
  }

  private ResourceLocator getBundle() {
    return this.bundle;
  }
}
