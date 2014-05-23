package org.silverpeas.components.quickinfo.model;

import java.util.List;

import com.silverpeas.pdc.model.PdcPosition;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public interface QuickInfoService {
  
  public String addNews(News news, List<PdcPosition> positions);
  
  public void updateNews(News news);
  
  public void removeNews(PublicationPK pk);
  
  public List<News> getAllNews(String componentId);
  
  public List<News> getVisibleNews(String componentId);
  
  public List<News> getPlatformNews(String userId);
  
  public News getANews(PublicationPK pk);

}
