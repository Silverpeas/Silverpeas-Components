package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
abstract class AbstractNewsResource extends RESTWebService {

  List<NewsEntity> asWebEntities(List<News> someNews) {
    List<NewsEntity> entities = new ArrayList<>();
    UriBuilder baseUri = getBaseUriBuilder();
    for (News news : someNews) {
      entities.add(asWebEntity(news, baseUri));
    }
    return entities;
  }

  NewsEntity asWebEntity(News news) {
    return asWebEntity(news, getBaseUriBuilder());
  }

  QuickInfoService getService() {
    return QuickInfoServiceProvider.getQuickInfoService();
  }

  private UriBuilder getBaseUriBuilder() {
    UriBuilder baseUri = getUriInfo().getBaseUriBuilder();
    baseUri.path("news/{componentId}/{newsId}");
    return baseUri;
  }

  private NewsEntity asWebEntity(News news, UriBuilder baseUri) {
    return NewsEntity.fromNews(news)
        .withURI(baseUri.build(news.getComponentInstanceId(), news.getId()));
  }
}