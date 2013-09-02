/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.mydb;

/**
 * MyDB constants.
 * @author Antoine HEDIN
 */
public interface MyDBConstants {

  /**
   * Actions.
   */
  public static final String ACTION_ADD_LINE = "AddLine";
  public static final String ACTION_CONNECTION_SETTING = "ConnectionSetting";
  public static final String ACTION_FILTER = "Filter";
  public static final String ACTION_MAIN = "Main";
  public static final String ACTION_TABLE_SELECTION = "TableSelection";
  public static final String ACTION_UPDATE_CONNECTION = "UpdateConnection";
  public static final String ACTION_UPDATE_DATA = "UpdateData";
  public static final String ACTION_UPDATE_LINE = "UpdateLine";
  public static final String ACTION_UPDATE_TABLE = "UpdateTable";

  /**
   * Pages.
   */
  public static final String PAGE_CONNECTION_SETTING = "connectionSetting.jsp";
  public static final String PAGE_CONSULTATION = "consultation.jsp";
  public static final String PAGE_FOREIGN_KEY = "foreignKey.jsp";
  public static final String PAGE_PRIMARY_KEY = "primaryKey.jsp";
  public static final String PAGE_TABLE_COLUMN = "tableColumn.jsp";
  public static final String PAGE_TABLE_LINE = "tableLine.jsp";
  public static final String PAGE_TABLE_SELECTION = "tableSelection.jsp";
  public static final String PAGE_TABLE_UPDATE = "tableUpdate.jsp";
  public static final String PAGE_UNICITY_KEY = "unicityKey.jsp";

}
