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

/**
 * Sets the data source field from the JdbcUrl field before later will be deleted
*/

import java.sql.SQLException

sql.eachRow('SELECT id, JDBCurl FROM SC_MyDB_ConnectInfo') { row ->
  String jdbcUrl = row.JDBCUrl
  if (jdbcUrl != null) {
    int idx = jdbcUrl.lastIndexOf('/')
    String dataSource = "java:/datasources/${jdbcUrl.substring(idx + 1)}"
    int count = sql.executeUpdate('UPDATE SC_MyDB_ConnectInfo SET dataSource = :dataSource WHERE id = :id',
        [id: row.id, dataSource: dataSource])
    if (count != 1) {
      throw new SQLException("Failed to update connection info of id = ${row.id} with data source = ${dataSource}")
    } else {
      log.info "Connection Info of id = ${row.id} -> set data source = ${dataSource}: [Ok]"
    }
  }
}
