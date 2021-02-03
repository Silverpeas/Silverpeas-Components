/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import java.sql.SQLException

/**
 * This script replace the value of the 'defaultView' application parameter for all the
 * rssAggregator instances. It replaces the misspelling 'AGREGATED' by 'AGGREGATED'.
 */

final def replaceValue = '''
  UPDATE st_instance_data SET value = REPLACE(value, 'AGREGATED', 'AGGREGATED') 
  WHERE componentid in (SELECT id FROM st_componentinstance WHERE componentname = 'rssAgregator')
'''

log.info 'Replace all the misspelling \'AGREGATED\' value to \'AGGREGATED\' for the \'defaultView\' parameter'

try {
  sql.executeUpdate replaceValue
} catch (Exception e) {
  throw new SQLException("Cannot replace 'defaultView' value for RSSAggretator applications", e)
}
