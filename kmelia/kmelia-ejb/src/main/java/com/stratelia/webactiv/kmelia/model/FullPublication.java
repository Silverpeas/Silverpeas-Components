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

package com.stratelia.webactiv.kmelia.model;

import java.util.List;

import com.stratelia.webactiv.util.publication.model.CompletePublication;

/**
 * This object contains elements which are displayed in a kmelia Topic
 * @author Nicolas Eysseric
 * @version 1.0
 * @deprecated this class is now deprecated. Please use instead the KmeliaPublication objects.
 */
@Deprecated
public class FullPublication extends Object implements java.io.Serializable {
  private static final long serialVersionUID = 7021715318778228441L;

  private List attachments;
  private CompletePublication publication;
  private List pdcPositions;

  public FullPublication() {
    init(null, null, null);
  }

  public FullPublication(CompletePublication publication, List attachments,
      List pdcPositions) {
    init(publication, attachments, pdcPositions);
  }

  private void init(CompletePublication publication, List attachments,
      List pdcPositions) {
    this.attachments = attachments;
    this.publication = publication;
    this.pdcPositions = pdcPositions;
  }

  public List getAttachments() {
    return this.attachments;
  }

  public CompletePublication getPublication() {
    return this.publication;
  }

  public void setAttachments(List attachments) {
    this.attachments = attachments;
  }

  public void setPublication(CompletePublication pub) {
    this.publication = pub;
  }

  public List getPdcPositions() {
    return pdcPositions;
  }

  public void setPdcPositions(List pdcPositions) {
    this.pdcPositions = pdcPositions;
  }
}