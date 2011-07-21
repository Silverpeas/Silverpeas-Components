/**
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SKDException;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

public class PhotoSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = -4373774805830276786L;
  static public DataFlavor PhotoDetailFlavor;

  static {
    try {
      PhotoDetailFlavor = new DataFlavor(Class
          .forName("com.silverpeas.gallery.model.PhotoDetail"), "Photo");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private PhotoDetail m_photo;

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Constructor
   * 
   */
  public PhotoSelection(PhotoDetail photo) {
    super();
    m_photo = photo;
    super.addFlavor(PhotoDetailFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;

    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (parFlavor.equals(PhotoDetailFlavor))
        transferedData = m_photo;
      else
        throw e;
    }
    return transferedData;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry;
    PhotoPK photoPK = m_photo.getPhotoPK();
    indexEntry = new IndexEntry(photoPK.getComponentName(), "Photo", m_photo
        .getPhotoPK().getId());
    indexEntry.setTitle(m_photo.getName());
    return indexEntry;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Tranformation obligatoire en
   * SilverpeasKeyData
   */
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();

    keyData.setTitle(m_photo.getName());
    keyData.setAuthor(m_photo.getCreatorId());
    keyData.setCreationDate(m_photo.getCreationDate());
    keyData.setDesc(m_photo.getDescription());
    try {
      keyData.setProperty("BEGINDATE", m_photo.getBeginDate().toString());
      keyData.setProperty("ENDDATE", m_photo.getEndDate().toString());
    } catch (SKDException e) {
      SilverTrace.error("gallery", "PhotoSelection.getKeyData",
          "gallery.ERROR_KEY_DATA", e);
    }
    return keyData;
  }

}
