package org.silverpeas.components.quickinfo.model;

import java.util.List;

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.pdc.model.PdcPosition;

public interface QuickInfoService extends SilverpeasComponentService<News> {
  
  public News create(News news);
  
  public void publish(String id, String userId);
  
  public void update(final News news, List<PdcPosition> positions, final boolean forcePublishing);
  
  public void removeNews(String id);
  
  public List<News> getAllNews(String componentId);
  
  public List<News> getVisibleNews(String componentId);
  
  public List<News> getPlatformNews(String userId);
  
  public News getNews(String id);
  
  public News getNewsByForeignId(String id);

}
