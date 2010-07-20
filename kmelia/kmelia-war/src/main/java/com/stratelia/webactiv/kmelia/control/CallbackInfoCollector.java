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

package com.stratelia.webactiv.kmelia.control;


import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CallbackInfoCollector extends HTMLEditorKit.ParserCallback {
  private List<String> table_columns;
  private int columns;
  private boolean first_row;

  CallbackInfoCollector() {
    table_columns = new ArrayList<String>(10);
  }

  public List<String> getTableColumnCount() {
    return Collections.unmodifiableList(table_columns);
  }

  @Override
  public void handleEndTag(Tag t, int pos) {
    SilverTrace.info("kmelia", "CallbackInfoCollector.handleEndTag", "root.MSG_ENTRY_METHOD", 
        "t = " + t.toString());
    if (Tag.TR.equals(t)) {
      if (first_row) {
        first_row = false;
        table_columns.add(String.valueOf(columns));
      }
    }
  }

  @Override
  public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
    SilverTrace.info("kmelia", "CallbackInfoCollector.handleStartTag", "root.MSG_ENTRY_METHOD", 
        "t = " + t.toString());
    if (Tag.TABLE.equals(t)) {
      columns = 0;
      first_row = true;
    } else if (Tag.TD.equals(t)) {
      columns++;
    }
  }
}
