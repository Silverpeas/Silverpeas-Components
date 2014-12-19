/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.kmelia.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationRuntimeException;
import org.silverpeas.publication.web.PublicationEntity;
import org.silverpeas.util.ServiceProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

/**
 * A REST Web resource allowing to update data related to a publication.
 */
@Service
@RequestScoped
@Path("publications/{componentId}")
@Authorized
public class KmeliaResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;
  
  @Override
  public String getComponentId() {
    return componentId;
  }
  
  /**
   * Creates a publication corresponding to the given publication entity and whose parent node
   * matches the specified node ID.
   * 
   * @param nodeId The ID of the publication's parent node.
   * @param publicationEntity The description of the publication to create.
   * @return
   */
  @Path("/{nodeId}")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response savePublication(@PathParam("nodeId") String nodeId,
      final PublicationEntity publicationEntity) {
    try {
      PublicationDetail publication = publicationEntity.toPublicationDetail();
      
      NodePK nodePK = getNodePK(nodeId);
      
      String pubId = getKmeliaBm().createPublicationIntoTopic(publication, nodePK);
      publication.getPK().setId(pubId);
      
      URI publicationURI = getUriInfo().getRequestUriBuilder().path(publication.getPK().getId())
          .build();
      return Response.created(publicationURI).
          entity(asWebEntity(publication, identifiedBy(publicationURI))).build();
    } catch (PublicationRuntimeException ex) {
      throw new WebApplicationException(ex, Status.CONFLICT);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  /**
   * Updates the publication described by the given publication entity.
   * 
   * @param publicationEntity The description of the publication to update.
   * @return a response containing the entity describing the updated publication.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updatePublication(final PublicationEntity publicationEntity) {
    try {
      PublicationDetail publication = publicationEntity.toPublicationDetail();
            
      getKmeliaBm().updatePublication(publication);
      
      URI publicationURI = getUriInfo().getRequestUriBuilder().path(publication.getPK().getId())
          .build();
      return Response.ok(publicationURI).
          entity(asWebEntity(publication, identifiedBy(publicationURI))).build();
    } catch (PublicationRuntimeException ex) {
      throw new WebApplicationException(ex, Status.CONFLICT);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  private NodePK getNodePK(String nodeId) {
    return new NodePK(nodeId, getComponentId());
  }
  
  private URI identifiedBy(URI uri) {
    return uri;
  }
  
  private PublicationEntity asWebEntity(final PublicationDetail publication, URI publicationURI) {
    return PublicationEntity.fromPublicationDetail(publication, publicationURI);
  }
  
  private KmeliaBm getKmeliaBm() {
    try {
      return ServiceProvider.getService(KmeliaBm.class);
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }
  }
  
}
