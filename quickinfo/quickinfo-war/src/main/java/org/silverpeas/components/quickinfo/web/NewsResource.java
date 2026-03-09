package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;

@WebService
@Path(AbstractNewsResource.PATH + "/{componentId}")
@Authorized
public class NewsResource extends AbstractNewsResource {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<NewsEntity> getAllNews() {
    final List<News> allNews;
    if (isContributor()) {
      allNews = getService().getAllNews(componentId);
    } else {
      allNews = getService().getVisibleNews(componentId);
    }
    return asWebEntities(allNews, true, 0);
  }
  
  @GET
  @Path("{newsId}")
  @Produces(MediaType.APPLICATION_JSON)
  public NewsEntity getNews(@PathParam("newsId") String onNewsId) {
    try {
      News news = getNewsById(onNewsId);
      return asWebEntity(news, true);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @POST
  @Path("{newsId}/acknowledge")
  public void acknowledge(@PathParam("newsId") String onNewsId) {
    try {
      getService().acknowledgeNews(onNewsId, getUser().getId());
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @DELETE
  @Path("{newsId}")
  public Response deleteNews(@PathParam("newsId") String onNewsId) {
    try {
      if (!isContributor()) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      getService().removeNews(onNewsId);
      return Response.ok().build();
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private News getNewsById(String id) {
    News news = getService().getNews(id);
    if (news == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return news;
  }

  private boolean isContributor() {
    return getHighestUserRole().isGreaterThanOrEquals(SilverpeasRole.PUBLISHER);
  }

}