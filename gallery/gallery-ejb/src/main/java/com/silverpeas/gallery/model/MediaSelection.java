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
package com.silverpeas.gallery.model;

import org.silverpeas.util.clipboard.ClipboardSelection;
import org.silverpeas.util.clipboard.SKDException;
import org.silverpeas.util.clipboard.SilverpeasKeyData;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.search.indexEngine.model.IndexEntry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

public class MediaSelection extends ClipboardSelection {

  private static final long serialVersionUID = -4373774805830276786L;
  static public DataFlavor MediaFlavor = new DataFlavor(Media.class, "Media");
  private Media currentMedia;


  public MediaSelection(Media media) {
    super();
    currentMedia = media;
    super.addFlavor(MediaFlavor);
  }

  @Override
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;

    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (MediaFlavor.equals(parFlavor)) {
        transferedData = currentMedia;
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  @Override
  public IndexEntry getIndexEntry() {
    MediaPK mediaPK = currentMedia.getMediaPK();
    IndexEntry indexEntry =
        new IndexEntry(mediaPK.getComponentName(), currentMedia.getType().name(),
            currentMedia.getMediaPK().getId());
    indexEntry.setTitle(currentMedia.getName());
    return indexEntry;
  }

  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();
    keyData.setTitle(currentMedia.getName());
    keyData.setAuthor(currentMedia.getCreatorId());
    keyData.setCreationDate(currentMedia.getCreationDate());
    keyData.setDesc(currentMedia.getDescription());
    try {
      if (currentMedia.getVisibilityPeriod().getBeginDatable().isDefined()) {
        keyData
            .setProperty("BEGINDATE", currentMedia.getVisibilityPeriod().getBeginDate().toString());
      }
      if (currentMedia.getVisibilityPeriod().getEndDatable().isDefined()) {
        keyData.setProperty("ENDDATE", currentMedia.getVisibilityPeriod().getEndDate().toString());
      }
    } catch (SKDException e) {
      SilverTrace.error("gallery", "PhotoSelection.getKeyData", "gallery.ERROR_KEY_DATA", e);
    }
    return keyData;
  }

}
