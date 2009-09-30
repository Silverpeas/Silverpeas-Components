package com.silverpeas.gallery.control;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;

public class GalleryInitialize implements IInitialize {
  public GalleryInitialize() {
  }

  public boolean Initialize() {
    ScheduledAlertUser sa = new ScheduledAlertUser();
    sa.initialize();
    ScheduledDeleteOrder so = new ScheduledDeleteOrder();
    so.initialize();
    return true;
  }
}