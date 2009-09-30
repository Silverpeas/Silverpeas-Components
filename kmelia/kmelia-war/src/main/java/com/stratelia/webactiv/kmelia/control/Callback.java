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
package com.stratelia.webactiv.kmelia.control;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;

class Callback extends HTMLEditorKit.ParserCallback {
  private Document document = null;
  private Table tbl;
  private Cell cl;
  private boolean is_was_text;
  private int font_properties;
  private int font_size;
  private Paragraph paragraph;
  private Vector columns;
  private int current_table;
  private Iterator textIterator;
  private Iterator imageIterator;

  private Object attribute;
  private String attribute_value;
  static private String imagePath = null;

  Callback(Document document, Vector columns, Iterator textIterator,
      Iterator imageIterator) {
    super();
    this.document = document;
    this.tbl = null;
    this.cl = null;
    this.font_properties = Font.NORMAL;
    this.font_size = Font.NORMAL;
    this.paragraph = null;
    this.columns = columns;
    this.current_table = -1;
    this.textIterator = textIterator;
    this.imageIterator = imageIterator;
  }

  public void handleEndTag(HTML.Tag t, int pos) throws KmeliaRuntimeException {
    SilverTrace.info("kmelia", "Callback.handleEndTag",
        "root.MSG_ENTRY_METHOD", "t = " + t.toString());
    if (t.equals(HTML.Tag.TABLE)) {
      try {
        if (tbl != null) {
          if (paragraph != null) {
            paragraph.add(tbl);
          } else {
            document.add(tbl);
          }
          tbl = null;
          cl = null;
        }
      } catch (Exception ex) {
        throw new KmeliaRuntimeException("Callback.handleEndTag",
            KmeliaRuntimeException.WARNING,
            "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
      }
    } else if (t.equals(HTML.Tag.CENTER) || t.equals(HTML.Tag.P)) {
      try {
        if (paragraph != null) {
          document.add(paragraph);
          paragraph = null;
        }
      } catch (Exception ex) {
        throw new KmeliaRuntimeException("Callback.handleEndTag",
            KmeliaRuntimeException.WARNING,
            "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
      }
    } else if (t.equals(HTML.Tag.TR)) {
      try {
        // if ( is_first_row )
        // {
        // is_first_row = false;
        // tbl.endHeaders();
        // }
      } catch (RuntimeException ex) {
        throw new KmeliaRuntimeException("Callback.handleEndTag",
            KmeliaRuntimeException.WARNING,
            "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
      }
    } else if (t.equals(HTML.Tag.TD)) {
      try {
        if (cl != null && tbl != null && is_was_text) {
          tbl.addCell(cl);
        }
      } catch (Exception ex) {
        throw new KmeliaRuntimeException("Callback.handleEndTag",
            KmeliaRuntimeException.WARNING,
            "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
      }
    } else if (t.equals(HTML.Tag.FONT)) {
      font_size = Font.NORMAL;
    } else if (t.equals(HTML.Tag.H1)) {
      font_size = Font.NORMAL;
    } else if (t.equals(HTML.Tag.H2)) {
      font_size = Font.NORMAL;
    } else if (t.equals(HTML.Tag.H3)) {
      font_size = Font.NORMAL;
    } else if (t.equals(HTML.Tag.H4)) {
      font_size = Font.NORMAL;
    } else if (t.equals(HTML.Tag.H5)) {
      font_size = Font.NORMAL;
    } else if (t.equals(HTML.Tag.H6)) {
      font_size = Font.NORMAL;
    } else if (t.equals(HTML.Tag.B)) {
      font_properties = Font.NORMAL;
    } else if (t.equals(HTML.Tag.I)) {
      font_properties = Font.NORMAL;
    } else if (t.equals(HTML.Tag.U)) {
      // current PDF package doesn't support it
    } else if (t.equals(HTML.Tag.BODY)) {
      try {
        if (cl != null && tbl != null) {
          tbl.addCell(cl);
          cl = null;
        }
        if (tbl != null) {
          if (paragraph != null) {
            paragraph.add(tbl);
          } else {
            document.add(tbl);
          }
        }
        if (paragraph != null) {
          document.add(paragraph);
        }
      } catch (Exception ex) {
        throw new KmeliaRuntimeException("Callback.handleEndTag",
            KmeliaRuntimeException.WARNING,
            "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
      }
    }
  }

  public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
      throws KmeliaRuntimeException {
    SilverTrace.info("kmelia", "Callback.handleSimpleTag",
        "root.MSG_ENTRY_METHOD", "t = " + t.toString());
    try {
      if (t.equals(HTML.Tag.BR) || t.equals(HTML.Tag.P)) {
        if (paragraph != null) {
          paragraph.add(new Paragraph("\n"));
        } else {
          document.add(new Paragraph("\n"));
        }
      }
    } catch (Exception ex) {
      throw new KmeliaRuntimeException("Callback.handleSimpleTag",
          KmeliaRuntimeException.WARNING,
          "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
    }
  }

  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
      throws KmeliaRuntimeException {
    SilverTrace.info("kmelia", "Callback.handleStartTag",
        "root.MSG_ENTRY_METHOD", "t = " + t.toString());
    if (t.equals(HTML.Tag.TABLE)) {
      try {
        tbl = new Table((new Integer((String) columns.get(++current_table)))
            .intValue());
        tbl.setBorderWidth(0);
        tbl.setAlignment(com.lowagie.text.Element.ALIGN_LEFT);
        attribute = a.getAttribute(HTML.Attribute.WIDTH);
        if (attribute != null) {
          attribute_value = attribute.toString();
          if ("%".equals(attribute_value
              .substring(attribute_value.length() - 1))) {
            tbl.setWidth((new Integer(attribute_value.substring(0,
                attribute_value.length() - 1))).intValue());
          }
        }
        attribute = a.getAttribute(HTML.Attribute.CELLPADDING);
        if (attribute != null) {
          attribute_value = attribute.toString();
          tbl.setSpacing((new Integer(attribute_value)).intValue());
        }
        attribute = a.getAttribute(HTML.Attribute.CELLSPACING);
        if (attribute != null) {
          attribute_value = attribute.toString();
          tbl.setPadding((new Integer(attribute_value)).intValue());
        }
      } catch (Exception ex) {
        throw new KmeliaRuntimeException("Callback.handleStartTag",
            KmeliaRuntimeException.WARNING,
            "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
      }
    }
    /*
     * doesn't work with chapter... else if ( t.equals(t.CENTER) ) { paragraph =
     * new Paragraph(); paragraph.setAlignment(Paragraph.ALIGN_CENTER); } else
     * if ( t.equals(t.P) ) { attribute = a.getAttribute( HTML.Attribute.ALIGN
     * ); if ( attribute != null ) { attribute_value = attribute.toString();
     * System.out.println("Try to create PARAGRAPH with " + attribute_value); if
     * ( "center".equals(attribute_value) ) { paragraph = new Paragraph();
     * paragraph.setAlignment(Paragraph.ALIGN_CENTER); } else if (
     * "right".equals(attribute_value) ) { paragraph = new Paragraph();
     * paragraph.setAlignment(Paragraph.ALIGN_RIGHT); } else if (
     * "left".equals(attribute_value) ) { paragraph = new Paragraph();
     * paragraph.setAlignment(Paragraph.ALIGN_LEFT); } } }
     */
    else if (t.equals(HTML.Tag.TD)) {
      try {
        if (cl == null) {
          cl = new Cell();
          cl.setBorderWidth(0);
        }
        is_was_text = false;
      } catch (Exception ex) {
        throw new KmeliaRuntimeException("Callback.handleStartTag",
            KmeliaRuntimeException.WARNING,
            "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
      }
    } else if (t.equals(HTML.Tag.H1)) {
      font_size = Font.DEFAULTSIZE + 1;
    } else if (t.equals(HTML.Tag.H2)) {
      font_size = Font.DEFAULTSIZE + 2;
    } else if (t.equals(HTML.Tag.H3)) {
      font_size = Font.DEFAULTSIZE + 3;
    } else if (t.equals(HTML.Tag.H4)) {
      font_size = Font.DEFAULTSIZE + 4;
    } else if (t.equals(HTML.Tag.H5)) {
      font_size = Font.DEFAULTSIZE + 5;
    } else if (t.equals(HTML.Tag.H6)) {
      font_size = Font.DEFAULTSIZE + 6;
    } else if (t.equals(HTML.Tag.FONT)) {
      attribute = a.getAttribute(HTML.Attribute.SIZE);
      if (attribute != null) {
        font_size = ((new Integer(attribute.toString())).intValue()
            + Font.DEFAULTSIZE - 1);
      }
    } else if (t.equals(HTML.Tag.B)) {
      font_properties = font_properties | Font.BOLD;
    } else if (t.equals(HTML.Tag.I)) {
      font_properties = font_properties | Font.ITALIC;
    } else if (t.equals(HTML.Tag.U)) {
      // current PDF package doesn't support it
    }
  }

  public void handleText(char[] data, int pos) throws KmeliaRuntimeException {
    SilverTrace.info("kmelia", "Callback.handleText", "root.MSG_ENTRY_METHOD",
        "data = " + data.toString());
    String toParse = new String(data);
    Font fnt = new Font(Font.UNDEFINED, font_size, font_properties);
    try {
      StringBuffer out = new StringBuffer(100);
      int posit = toParse.indexOf("%WA");
      while (posit != -1) {
        if (posit > 0) {
          out.append(toParse.substring(0, posit));
          toParse = toParse.substring(posit);
        }
        if (toParse.startsWith("%WATXTDATA%")) {
          if (textIterator.hasNext()) {
            InfoTextDetail textDetail = (InfoTextDetail) textIterator.next();
            out.append(textDetail.getContent());
          } else {
            out.append(" ");
          }
          toParse = toParse.substring(11);
        } else if (toParse.startsWith("%WAIMGDATA%")) {
          if (imageIterator.hasNext()) {
            if (out.length() > 0) {
              addText(out.toString(), fnt);
            }
            out.delete(0, out.length());
            InfoImageDetail imageDetail = (InfoImageDetail) imageIterator
                .next();

            String imagePath = FileRepositoryManager
                .getAbsolutePath(imageDetail.getPK().getComponentName())
                + getImagePath()
                + File.separator
                + imageDetail.getPhysicalName();
            SilverTrace.info("kmelia", "Callback.handleText",
                "root.MSG_PARAM_VALUE", "imagePath = " + imagePath.toString());
            com.lowagie.text.Image image = com.lowagie.text.Image
                .getInstance(imagePath);

            document.add(image);
          }
          toParse = toParse.substring(11);
        } else {
          // we have to do this for avoid infinite loop i case
          // wrong tag
          posit = toParse.indexOf("%", 2);
          if (posit >= 0) {
            toParse = toParse.substring(posit + 1);
          } else {
            toParse = toParse.substring(1);
          }
        }
        posit = toParse.indexOf("%WA");
      }
      if (toParse.length() > 0) {
        out.append(toParse);
      }

      addText(out.toString(), fnt);
    } catch (Exception ex) {
      throw new KmeliaRuntimeException("Callback.handleText",
          KmeliaRuntimeException.WARNING,
          "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
    }
  }

  private String escapeChars(String text) throws KmeliaRuntimeException {
    /*
     * int pos = text.indexOf('\n'); while ( pos >= 0 ) { text =
     * text.substring(0, pos) + text.substring(pos+1); pos = text.indexOf('\n');
     * }
     */
    int pos = text.indexOf('\r');
    while (pos >= 0) {
      text = text.substring(0, pos) + text.substring(pos + 1);
      pos = text.indexOf('\r');
    }
    return text;
  }

  private void addText(String text, Font fnt) throws KmeliaRuntimeException {
    SilverTrace.info("kmelia", "Callback.addText", "root.MSG_ENTRY_METHOD",
        "text = " + text);
    try {
      text = escapeChars(text);
      if (cl != null) {
        cl = new Cell(new Chunk(text, fnt));
        cl.setBorderWidth(0);
        is_was_text = true;
      } else if (paragraph != null) {
        paragraph.add(text);
        // paragraph.add( new Chunk( text, fnt ) );
      } else {
        document.add(new Paragraph(text));
        paragraph = null;
      }
    } catch (Exception ex) {
      throw new KmeliaRuntimeException("Callback.addText",
          KmeliaRuntimeException.WARNING,
          "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
    }
  }

  private static void initRessources() {
    ResourceLocator pubSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.publication.publicationSettings", "");
    imagePath = pubSettings.getString("imagesSubDirectory");
  }

  private static String getImagePath() {
    if (imagePath == null) {
      initRessources();
    }
    return imagePath;
  }
}
