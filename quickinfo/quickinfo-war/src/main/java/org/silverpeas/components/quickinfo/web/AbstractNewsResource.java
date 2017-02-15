package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 03/03/2017.
 */
public abstract class AbstractNewsResource extends RESTWebService {

  protected UriBuilder getBaseUri() {
    UriBuilder baseUri = getUriInfo().getBaseUriBuilder();
    baseUri.path("news/{componentId}/{newsId}");
    return baseUri;
  }

  protected List<NewsEntity> asWebEntities(List<News> someNews) {
    List<NewsEntity> entities = new ArrayList<>();
    UriBuilder baseUri = getBaseUri();
    for (News news : someNews) {
      entities.add(asWebEntity(news, baseUri));
    }
    return entities;
  }

  protected NewsEntity asWebEntity(News news, UriBuilder baseUri) {
    return NewsEntity.fromNews(news)
        .withURI(baseUri.build(news.getComponentInstanceId(), news.getId()));
  }

  protected NewsEntity asWebEntity(News news) {
    return asWebEntity(news, getBaseUri());
  }

  protected QuickInfoService getService() {
    return QuickInfoServiceProvider.getQuickInfoService();
  }

}