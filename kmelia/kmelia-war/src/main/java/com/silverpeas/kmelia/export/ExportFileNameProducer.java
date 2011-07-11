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
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.export;

import com.stratelia.webactiv.kmelia.model.KmeliaPublication;

/**
 * A producer of names of files used in the export process.
 * The producer of file names encapsulates the strategy used in the generation of file names for the
 * publications export.
 * 
 * The produce of file names depends not only on the publication to export in the file but also on
 * the context within which the export will occur. So that, a producer is get for a specific
 * Kmelia session controller that wraps that context.
 */
public interface ExportFileNameProducer {
  
  /**
   * Gets the name of the file into which the specified publication can be exported, whatever the
   * format of the export file.
   * @param publication the Kmelia publication to export.
   * @param language the language in which the file should be named. This parameter can be not taken
   * into account in the file name computation by the implementation (depends on the strategy used
   * in the name production).
   * @return the name of the file into which the publication can be exported.
   */
  String getPublicationExportFileName(final KmeliaPublication publication, String language);
  
}
