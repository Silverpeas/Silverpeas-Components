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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.node.web.NodeEntity;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;

import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;

/**
 * A REST Web resource representing a given node. It is a web service that provides an access to a
 * node referenced by its URL.
 */
@Service
@RequestScoped
@Authorized
@Path("folders/{componentId}")
public class FolderResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  /**
   * Get the root of the application and its children.
   *
   * @return the application root and its children
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity getRoot(@QueryParam("lang") String language) {
    NodeDetail root = null;
    try {
      root = getKmeliaBm().getRoot(componentId, getUserDetail().getId());
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    URI uri = getUriInfo().getRequestUriBuilder().path(root.getNodePK().getId()).build();
    if (getUriInfo().getRequestUri().toString().endsWith("/" + NodePK.ROOT_NODE_ID)) {
      uri = getUriInfo().getRequestUri();
    }
    NodeEntity entity = NodeEntity.fromNodeDetail(root, uri, language);
    decorateRoot(entity);

    return entity;
  }

  /**
   * Get any node of the application and its children.
   *
   * @return NodeEntity representing asking node
   */
  @GET
  @Path("{path: [0-9]+(/[0-9]+)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity getNode(@PathParam("path") String path, @QueryParam("lang") String language) {
    String nodeId = getNodeIdFromURI(path);
    if (nodeId.equals(NodePK.ROOT_NODE_ID)) {
      return getRoot(language);
    } else {
      NodeDetail node = getNodeDetail(nodeId);
      URI uri = getUriInfo().getRequestUri();
      return NodeEntity.fromNodeDetail(node, uri, language);
    }
  }

  /**
   * Get all children of any node of the application.
   *
   * @return an array of NodeEntity representing children
   */
  @GET
  @Path("{path: [0-9]+(/[0-9]+)*/path}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity[] getPath(@PathParam("path") String path, @QueryParam("lang") String language) {
    String[] nodeIds = path.split("/");
    String nodeId = nodeIds[nodeIds.length - 2];
    NodePK nodePK = new NodePK(nodeId, componentId);

    List<NodeDetail> nodes;
    try {
      nodes = new ArrayList<NodeDetail>(getNodeBm().getPath(nodePK));
      Collections.reverse(nodes);

      String requestUri = getUriInfo().getRequestUri().toString();
      String uri = requestUri.substring(0, requestUri.lastIndexOf("/"));

      List<NodeEntity> entities = new ArrayList<NodeEntity>();
      for (NodeDetail node : nodes) {
        entities.add(NodeEntity.fromNodeDetail(node, uri, language));
      }

      return entities.toArray(new NodeEntity[0]);
    } catch (Exception e1) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Get all children of any node of the application.
   *
   * @return an array of NodeEntity representing children
   */
  @GET
  @Path("{path: [0-9]+(/[0-9]+)*/children}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity[] getChildren(@PathParam("path") String path,
      @QueryParam("lang") String language) {
    String[] nodeIds = path.split("/");
    String nodeId = nodeIds[nodeIds.length - 2];
    NodePK nodePK = new NodePK(nodeId, componentId);

    Collection<NodeDetail> children;
    try {
      children = getKmeliaBm().getFolderChildren(nodePK, getUserDetail().getId());
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    String requestUri = getUriInfo().getRequestUri().toString();
    String uri = requestUri.substring(0, requestUri.lastIndexOf("/"));

    List<NodeEntity> entities = new ArrayList<NodeEntity>();
    for (NodeDetail child : children) {
      entities.add(NodeEntity.fromNodeDetail(child, uri, language));
    }

    NodeEntity[] aEntities = entities.toArray(new NodeEntity[0]);

    if (nodePK.isRoot()) {
      // decorate special nodes
      decorateRootChildren(aEntities);
    }

    return aEntities;
  }

  private void decorateRoot(NodeEntity root) {
    root.setState("open");
    root.getAttr().setRel("root");
    decorateRootChildren(root.getChildren());
  }

  private void decorateRootChildren(NodeEntity[] children) {
    // case of special nodes (bin, to validate)
    for (NodeEntity child : children) {
      if (child.getAttr().getId().equalsIgnoreCase("tovalidate")) {
        child.getAttr().setRel("tovalidate");
        child.setState("");
      } else if (child.getAttr().getId().equalsIgnoreCase("1")) {
        child.getAttr().setRel("bin");
        child.setState("");
      }
    }
  }

  /**
   * Get all children of any node of the application.
   *
   * @return an array of NodeEntity representing children
   */
  @GET
  @Path("{path: [0-9]+/treeview}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity getTreeview(@PathParam("path") String path, @QueryParam("lang") String language) {
    String[] nodeIds = path.split("/");
    String nodeId = nodeIds[nodeIds.length - 2];

    try {
      List<NodeDetail> nodes = new ArrayList<NodeDetail>(getNodeBm().getPath(new NodePK(nodeId,
          componentId)));
      Collections.reverse(nodes);
      NodeDetail root = getKmeliaBm().getExpandedPathToNode(new NodePK(nodeId, componentId),
          getUserDetail().getId());

      String requestUri = getUriInfo().getRequestUri().toString();
      String uri = requestUri.substring(0, requestUri.lastIndexOf("/"));

      NodeEntity rootEntity = NodeEntity.fromNodeDetail(root, uri, language);
      decorateRoot(rootEntity);

      setOpenState(rootEntity.getChildren(), nodes);

      return rootEntity;
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  private void setOpenState(NodeEntity[] children, List<NodeDetail> path) {
    for (NodeEntity child : children) {
      if (isInPath(child, path)) {
        child.setState("open");
        setOpenState(child.getChildren(), path);
      }
    }
  }

  private boolean isInPath(NodeEntity entity, List<NodeDetail> path) {
    for (NodeDetail node : path) {
      if (node.getNodePK().getId().equals(entity.getAttr().getId())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  private String getNodeIdFromURI(String uri) {
    String[] nodeIds = uri.split("/");
    return nodeIds[nodeIds.length - 1];
  }

  private NodeDetail getNodeDetail(String id) {
    try {
      return getNodeBm().getDetail(getNodePK(id));
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, getComponentId());
  }

  private NodeBm getNodeBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBm.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  public KmeliaBm getKmeliaBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }
}