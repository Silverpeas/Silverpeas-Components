/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.kmelia.web;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.owasp.encoder.Encode;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.webapi.node.NodeAttrEntity;
import org.silverpeas.core.webapi.node.NodeEntity;
import org.silverpeas.core.webapi.node.NodeType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A REST Web resource representing a given node. It is a web service that provides an access to a
 * node referenced by its URL.
 */
@WebService
@Authorized
@Path(FolderResource.PATH + "/{componentId}")
public class FolderResource extends RESTWebService {

  static final String PATH = "folders";

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
    NodeDetail root;
    try {
      root = getKmeliaService().getRoot(componentId, getUser().getId());
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }

    final URI uri;
    if (getUri().getRequestUri().toString().endsWith("/" + NodePK.ROOT_NODE_ID)) {
      uri = getUri().getRequestUri();
    } else {
      uri = getUri().getRequestUriBuilder().path(root.getNodePK().getId()).build();
    }

    return asNodeEntity(root, uri, language);
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
    NodeDetail node = getKmeliaService().getFolder(getNodePK(nodeId), getUser().getId());
    URI uri = getUri().getRequestUri();
    return asNodeEntity(node, uri, language);
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
      nodes = new ArrayList<>(getNodeService().getPath(nodePK));
      Collections.reverse(nodes);

      return asNodeEntities(nodes, language, nodePK.isTrash());
    } catch (Exception e1) {
      throw new WebApplicationException(e1, Status.INTERNAL_SERVER_ERROR);
    }
  }

  @NotNull
  private NodeEntity[] asNodeEntities(final Collection<NodeDetail> nodes, final String language,
      final boolean decorate) {
    String requestUri = getUri().getRequestUri().toString();
    String uri = requestUri.substring(0, requestUri.lastIndexOf('/'));

    List<NodeEntity> entities = new ArrayList<>();
    final SilverpeasRole highestUserRole = getHighestUserRoleIfAny();
    for (NodeDetail node : nodes) {
      entities.add(NodeEntity.fromNodeDetail(highestUserRole, node, uri, language));
    }

    return decorate ? decorateRootChildren(entities, language) : entities.toArray(new NodeEntity[0]);
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

    try {
      Collection<NodeDetail> children =
          getKmeliaService().getFolderChildren(nodePK, getUser().getId());
      return asNodeEntities(children, language, nodePK.isRoot());
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Creates a node corresponding to the given node entity and whose parent matches the specified
   * path.
   *
   * @param path The path of the parent node.
   * @param nodeEntity The description of the node to create.
   * @return a response containing the entity describing the newly created node.
   */
  @Path("{path: [0-9]+(/[0-9]+)*}")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createNode(@PathParam("path") String path, final NodeEntity nodeEntity) {
    String parentNodeId = getNodeIdFromURI(path);
    NodePK nodePK = getNodePK(parentNodeId);

    String nodeName = nodeEntity.getText();
    if (StringUtils.isEmpty(nodeName)) {
      throw new WebApplicationException(Status.NO_CONTENT);
    }

    Collection<NodeDetail> children;
    try {
      children = getKmeliaService().getFolderChildren(nodePK, getUser().getId());
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }

    if (children != null) {
      for (NodeDetail node : children) {
        if (nodeName.equals(node.getName())) {
          throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        }
      }
    }

    NodeAttrEntity nodeAttr = nodeEntity.getAttr();
    String userId = nodeAttr.getCreatorId();
    if (StringUtils.isEmpty(userId) && nodeAttr.getCreator() != null) {
      userId = nodeAttr.getCreator().getId();
    }
    String description = nodeAttr.getDescription();

    try {
      String nodeId = getKmeliaService().createTopic(
          componentId, parentNodeId, null, userId, nodeName, description);

      NodeDetail node = getNodeDetail(nodeId);
      URI uri = getUri().getRequestUri();
      NodeEntity newNodeEntity = NodeEntity.fromNodeDetail(getHighestUserRoleIfAny(), node, uri);

      return Response.created(uri).entity(newNodeEntity).build();
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private void decorateRoot(NodeEntity root, String lang) {
    root.getState().setOpened(true);
    root.setType(NodeType.ROOT);
    decorateRootChildren(Arrays.asList(root.getChildren()), lang);
  }

  private NodeEntity[] decorateRootChildren(List<NodeEntity> children, String lang) {
    // case of special nodes (bin, to validate)
    LocalizationBundle messages =
        ResourceLocator.getLocalizationBundle("org.silverpeas.kmelia.multilang.kmeliaBundle", lang);
    for (NodeEntity child : children) {
      if (child.getAttr().getId().equalsIgnoreCase("tovalidate")) {
        child.setType(NodeType.TO_VALIDATE);
        child.getState().opened(false).setSelected(false);
        child.setText(Encode.forHtml(messages.getString("ToValidateShort")));
        child.getAttr().setDescription(messages.getString("kmelia.tovalidate.desc"));
      } else if (child.getAttr().getId().equalsIgnoreCase("1")) {
        child.setType(NodeType.BIN);
        child.getState().opened(false).setSelected(false);
        child.setText(Encode.forHtml(messages.getString("kmelia.basket")));
        child.getAttr().setDescription(messages.getString("kmelia.basket.desc"));
      }
    }
    return children.toArray(new NodeEntity[0]);
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
      List<NodeDetail> nodes = new ArrayList<>(getNodeService().getPath(new NodePK(nodeId,
          componentId)));
      Collections.reverse(nodes);
      NodeDetail root = getKmeliaService().getExpandedPathToNode(new NodePK(nodeId, componentId),
          getUser().getId());

      String requestUri = getUri().getRequestUri().toString();
      String uri = requestUri.substring(0, requestUri.lastIndexOf('/'));

      NodeEntity rootEntity = NodeEntity.fromNodeDetail(getHighestUserRoleIfAny(), root, uri, language);
      decorateRoot(rootEntity, language);

      setOpenState(rootEntity.getChildren(), nodes);

      return rootEntity;
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }
  }

  private void setOpenState(NodeEntity[] children, List<NodeDetail> path) {
    for (NodeEntity child : children) {
      if (isInPath(child, path)) {
        child.getState().setOpened(true);
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
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  private String getNodeIdFromURI(String uri) {
    String[] nodeIds = uri.split("/");
    return nodeIds[nodeIds.length - 1];
  }

  private NodeEntity asNodeEntity(final NodeDetail node, final URI uri, final String language) {
    final NodeEntity entity = NodeEntity.fromNodeDetail(getHighestUserRoleIfAny(), node, uri, language);
    if (node.isRoot()) {
      decorateRoot(entity, language);
    }
    return entity;
  }

  private NodeDetail getNodeDetail(String id) {
    try {
      return getNodeService().getDetail(getNodePK(id));
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, getComponentId());
  }

  private NodeService getNodeService() {
    try {
      return NodeService.get();
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }
  }

  private KmeliaService getKmeliaService() {
    try {
      return KmeliaService.get();
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
    }
  }

  private SilverpeasRole getHighestUserRoleIfAny() {
    return getUser() != null && getComponentId() != null ? getHighestUserRole() : null;
  }
}