/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.mydb.web;

import org.silverpeas.components.mydb.model.DbColumn;
import org.silverpeas.components.mydb.model.TableRow;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.SelectableUIEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author silveryocha
 */
public class TableRowUIEntity extends SelectableUIEntity<TableRow> {

  private final String uiId = UUID.randomUUID().toString();
  private final List<String> pkFields;
  private String pkValue;
  private String json;

  private TableRowUIEntity(final List<String> pkFields, final TableRow data, final Set<String> selectedIds) {
    super(data, selectedIds);
    this.pkFields = pkFields;
  }

  @Override
  public String getId() {
    return uiId;
  }

  public String getPkValue() {
    if (pkValue == null) {
      pkValue = pkFields.stream()
          .map(f -> f + "-" + getData().getFieldValue(f).toString())
          .collect(Collectors.joining("-"));
    }
    return pkValue;
  }

  public String toJSON() {
    if (json == null) {
      json = JSONCodec.encodeObject(o -> {
        getData().getFields().forEach((f, v) ->
            o.put(f, v.toString()));
        return o;
      });
    }
    return json;
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item wrapping the {@link
   * TableRow}.
   * @param tableView the view on the current database table to display.
   * @param values the list of all the {@link TableRow} of the database table.
   * @param selectedIds a set of identifiers to indicate what rows has to be selected when rendering
   * the table view.
   * @return the {@link SilverpeasList} of {@link TableRowUIEntity}.
   */
  static <U extends TableRow> SilverpeasList<TableRowUIEntity> convertList(
      final TableView tableView, final SilverpeasList<U> values, final Set<String> selectedIds) {
    final List<String> pkFields = tableView.getColumns()
        .stream().filter(DbColumn::isPrimaryKey)
        .map(DbColumn::getName)
        .collect(Collectors.toList());
    final Function<TableRow, TableRowUIEntity> converter = c -> new TableRowUIEntity(pkFields, c, selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }
}
