package com.silverpeas.gallery.model;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SKDException;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;

public class PhotoSelection extends ClipboardSelection implements Serializable {

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
  private ModelDetail m_modelDetail;
  private InfoDetail m_infoDetail;

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
