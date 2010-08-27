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
package com.stratelia.webactiv.almanach.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;

public class PeriodicityDAO {

  private static final String COLUMNNAMES =
      "id, eventid, unity, frequency, daysweekbinary, numweek, day, untildateperiod";
  private static final String TABLENAME = "SC_Almanach_Periodicity";

  public static Collection<Integer> getPeriodicEventIds(Connection con, String periodStart)
      throws SQLException, Exception {
    ResultSet rs = null;
    PreparedStatement selectStmt = null;

    String selectQuery =
        "select eventid from " + TABLENAME +
            " where untildateperiod >= ? " +
            " or untildateperiod is null";

    try {
      SilverTrace.info("almanach", "EventDAO.getAllEvents()",
          "almanach.MSG_SQL_REQUEST", "selectRequest = " + selectQuery);

      selectStmt = con.prepareStatement(selectQuery);
      selectStmt.setString(1, periodStart);
      rs = selectStmt.executeQuery();
      List<Integer> list = new ArrayList<Integer>();
      while (rs.next()) {
        list.add(rs.getInt(1));
      }
      return list;
    } finally {
      DBUtil.close(rs, selectStmt);
    }
  }

}
