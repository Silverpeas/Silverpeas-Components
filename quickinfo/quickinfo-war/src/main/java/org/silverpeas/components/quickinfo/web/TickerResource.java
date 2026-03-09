package org.silverpeas.components.quickinfo.web;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
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