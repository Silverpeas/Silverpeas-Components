
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
package com.silverpeas.gallery.model;



public class PhotoWithStatus {
  
  private PhotoDetail photo;
  private boolean update;
  
  public PhotoWithStatus(PhotoDetail photo, boolean update) {
    this.photo = photo;
    this.update = update;
  }

  public PhotoDetail getPhoto() {
    return photo;
  }

  public boolean isUpdate() {
    return update;
  }

  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((photo == null) ? 0 : photo.hashCode());
    result = prime * result + (update ? 1231 : 1237);
    return result;
  }

  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PhotoWithStatus)) {
      return false;
    }
    PhotoWithStatus other = (PhotoWithStatus) obj;
    if (photo == null) {
      if (other.photo != null) {
        return false;
      }
    } else if (!photo.equals(other.photo)) {
      return false;
    }
    return update == other.update;
  }

  
    
  

}
