package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Service
@RequestScoped
@Path("news/{componentId}")
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
    List<News> allNews = null;
    if (isContributor()) {
      allNews = getService().getAllNews(componentId);
    } else {
      allNews = getService().getVisibleNews(componentId);
    }
    return asWebEntities(allNews);
  }
  
  @GET
  @Path("{newsId}")
  @Produces(MediaType.APPLICATION_JSON)
  public NewsEntity getNews(@PathParam("newsId") String onNewsId) {
    try {
      News news = getNewsById(onNewsId);
      return asWebEntity(news);
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
    return getGreaterUserRole().isGreaterThanOrEquals(SilverpeasRole.publisher);
  }

}