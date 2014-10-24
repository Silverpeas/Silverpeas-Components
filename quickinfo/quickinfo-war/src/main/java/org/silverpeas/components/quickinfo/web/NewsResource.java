package org.silverpeas.components.quickinfo.web;

import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;
import com.silverpeas.web.UserPrivilegeValidation;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequestScoped
@Path("news")
@Authenticated
public class NewsResource extends RESTWebService {

  @Override
  public String getComponentId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation)
      throws WebApplicationException {
    super.validateUserAuthentication(
        validation.skipLastUserAccessTimeRegistering(getHttpServletRequest()));
  }

  @GET
  @Path("ticker")
  @Produces(MediaType.APPLICATION_JSON)
  public List<NewsEntity> getTickerNews() {
    List<NewsEntity> entities = new ArrayList<NewsEntity>();
    
    List<News> newsForTicker = getService().getNewsForTicker(getUserDetail().getId());
    for (News news : newsForTicker) {
      entities.add(NewsEntity.fromNews(news));
    }
    
    return entities;
  }
  
  @GET
  @Path("{newsId}")
  @Produces(MediaType.APPLICATION_JSON)
  public NewsEntity getNews(@PathParam("newsId") String onNewsId) {
    try {
      News news = getService().getNews(onNewsId);
      URI newsURI = getUriInfo().getRequestUri();
      return asWebEntity(news, newsURI);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  @POST
  @Path("{newsId}/acknowledge")
  public void acknowledge(@PathParam("newsId") String onNewsId) {
    try {
      getService().acknowledgeNews(onNewsId, getUserDetail().getId());
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  protected NewsEntity asWebEntity(final News news, URI newsURI) {
    return NewsEntity.fromNews(news).withURI(newsURI);
  }
  
  private QuickInfoService getService() {
    return QuickInfoServiceFactory.getQuickInfoService();
  }

}
