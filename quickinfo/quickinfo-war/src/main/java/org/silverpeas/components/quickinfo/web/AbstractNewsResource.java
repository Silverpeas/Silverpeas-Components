package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.core.web.rs.RESTWebService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Nicolas Eysseric
 */
abstract class AbstractNewsResource extends RESTWebService {

  static final String PATH = "news";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  List<NewsEntity> asWebEntities(List<News> someNews, final boolean withExtraInfo, final int limit) {
    Stream<NewsEntity> newsEntityStream = someNews.stream().map(n -> asWebEntity(n, withExtraInfo));
    if (limit > 0) {
      newsEntityStream = newsEntityStream.limit(limit);
    }
    return newsEntityStream.collect(Collectors.toList());
  }

  NewsEntity asWebEntity(final News news, final boolean withExtraInfo) {
    final NewsEntity entity = NewsEntity.fromNews(news);
    if (withExtraInfo) {
      entity.setExtraInfo(news);
    }
    return entity.withURI(getUri().getWebResourcePathBuilder().path(news.getId()).build());
  }

  QuickInfoService getService() {
    return QuickInfoServiceProvider.getQuickInfoService();
  }
}