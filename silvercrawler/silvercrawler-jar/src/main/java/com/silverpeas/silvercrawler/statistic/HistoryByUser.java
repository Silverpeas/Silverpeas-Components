/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
---*/

package com.silverpeas.silvercrawler.statistic;

import java.io.Serializable;
import java.util.Date;

import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class HistoryByUser implements Serializable {

  private static final long serialVersionUID = -5874285806646269018L;
  private UserDetail user;
  private Date lastDownload;
  private int nbDownload;

  public HistoryByUser(UserDetail user, Date lastDownload, int nbDownload) {
    this.lastDownload = lastDownload;
    this.user = user;
    this.nbDownload = nbDownload;
  }

  public Date getLastDownload() {
    return lastDownload;
  }

  public UserDetail getUser() {
    return user;
  }

  public int getNbDownload() {
    return nbDownload;
  }

  public void setLastDownload(Date lastDownload) {
    this.lastDownload = lastDownload;
  }

  public void setNbDownload(int nbDownload) {
    this.nbDownload = nbDownload;
  }

  public void setUser(UserDetail user) {
    this.user = user;
  }

}
