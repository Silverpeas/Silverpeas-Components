package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.core.webapi.base.RESTWebService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
abstract class AbstractNewsResource extends RESTWebService {

  static final String PATH = "news";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  List<NewsEntity> asWebEntities(List<News> someNews) {
    List<NewsEntity> entities = new ArrayList<>();
    for (News news : someNews) {
      entities.add(asWebEntity(news));
    }
    return entities;
  }

  NewsEntity asWebEntity(News news) {
    return NewsEntity.fromNews(news)
        .withURI(getUri().getWebResourcePathBuilder().path(news.getId()).build());
  }

  QuickInfoService getService() {
    return QuickInfoServiceProvider.getQuickInfoService();
  }
}