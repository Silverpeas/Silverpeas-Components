package com.silverpeas.silvercrawler.control;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;

public class SilverCrawlerInitialize implements IInitialize {
  public SilverCrawlerInitialize() {
  }

  public boolean Initialize() {
    ScheduledIndexFiles sif = new ScheduledIndexFiles();
    sif.initialize();
    return true;
  }
}