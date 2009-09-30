package com.stratelia.webactiv.kmelia.control;

import java.util.Vector;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

class CallbackInfoCollector extends HTMLEditorKit.ParserCallback {
  private Vector table_columns;
  private int columns;
  private boolean first_row;

  CallbackInfoCollector() {
    table_columns = new Vector(10);
  }

  public Vector getTableColumnCount() {
    return table_columns;
  }

  public void handleEndTag(HTML.Tag t, int pos) {
    SilverTrace.info("kmelia", "CallbackInfoCollector.handleEndTag",
        "root.MSG_ENTRY_METHOD", "t = " + t.toString());
    if (t.equals(HTML.Tag.TR)) {
      if (first_row) {
        first_row = false;
        table_columns.add(String.valueOf(columns));
      }
    }
  }

  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    SilverTrace.info("kmelia", "CallbackInfoCollector.handleStartTag",
        "root.MSG_ENTRY_METHOD", "t = " + t.toString());
    if (t.equals(HTML.Tag.TABLE)) {
      columns = 0;
      first_row = true;
    } else if (t.equals(HTML.Tag.TD)) {
      columns++;
    }
  }
}
