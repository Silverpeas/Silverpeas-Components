package migrations.scripts.community.up002
/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * st_space and st_group tables have been updated with additional properties in migration
 * version 048 of the busCore module. We have then to update the rows with values from the
 * st_community table. Because the database migrations of Silverpeas Core are executed first, for
 * old versions of Silverpeas, the table st_community couldn't been yet created - this is why the
 * update of the data in the st_space and st_group tables is done here.
 */

// we prefer here to split the change into two SQL requests instead of using a join one for
// stability and database-dialect agnostic reasons
def communities = sql.rows('SELECT groupId, spaceId from sc_community')
communities.each { community ->
    String spaceId = community.spaceId.substring(2)
    sql.executeUpdate("UPDATE st_space SET isCommunity = ? WHERE id = ?",
            [1, Integer.parseInt(spaceId)])
    sql.executeUpdate("UPDATE st_group SET spaceId = ? WHERE id = ?",
            [community.spaceId, community.groupId])
}
