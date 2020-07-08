package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@WebService
@Path(AbstractNewsResource.PATH + "/ticker")
@Authenticated
public class TickerResource extends AbstractNewsResource {

  @Override
  public String getComponentId() {
    return null;
  }

  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation) {
    super.validateUserAuthentication(
        validation.skipLastUserAccessTimeRegistering(getHttpServletRequest()));
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<NewsEntity> getTickerNews(final @QueryParam("limit") Integer limit) {
    final List<News> newsForTicker = getService().getNewsForTicker(getUser().getId());
    return asWebEntities(newsForTicker, false, limit != null ? limit : 0);
  }
}