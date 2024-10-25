/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.util.MapUtil;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

@Named
public class KmeliaComponentAuthorization implements ComponentAuthorization {

  private static final String PUBLICATION_TYPE = "Publication";
  private static final String NODE_TYPE = "Node";
  private static final String ATTACHMENT_TYPE = "Attachment";
  private static final String VERSION_TYPE = "Version";

  private KmeliaComponentAuthorization() {
    // Instantiated by IoC only.
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("kmelia");
  }

  @Override
  public <T> Stream<T> filter(final Collection<T> resources,
      final Function<T, ComponentResourceReference> converter, final String userId,
      final AccessControlOperation... operations) {
    final Map<ResourceRefHash, Set<ComponentResourceReference>> pubPks =
        new HashMap<>(resources.size());
    final Map<ResourceRefHash, Set<ComponentResourceReference>> nodePks =
        new HashMap<>(resources.size());
    final Map<String, Set<ComponentResourceReference>> instanceIds =
        new HashMap<>(resources.size());
    final Set<ComponentResourceReference> authorized = new HashSet<>(resources.size());
    resources.forEach(r -> {
      final ComponentResourceReference resourceRef = converter.apply(r);
      final String resourceType = resourceRef.getType();
      if (isHandledKmeliaResourceType(resourceType)) {
        if (StringUtil.isLong(resourceRef.getLocalId())) {
          MapUtil.putAddSet(pubPks,
              new ResourceRefHash(resourceRef.getLocalId(), resourceRef.getInstanceId()),
              resourceRef);
        }
      } else if (NODE_TYPE.equalsIgnoreCase(resourceType)) {
        MapUtil.putAddSet(nodePks,
            new ResourceRefHash(resourceRef.getLocalId(), resourceRef.getInstanceId()),
            resourceRef);
      } else if (isDefined(resourceRef.getInstanceId())) {
        MapUtil.putAddSet(instanceIds, resourceRef.getInstanceId(), resourceRef);
      } else {
        authorized.add(resourceRef);
      }
    });
    PublicationAccessControl.get()
        .filterAuthorizedByUser(ResourceRefHash.toPublicationPKs(pubPks.keySet()), userId,
            AccessControlContext.init().onOperationsOf(operations))
        .forEach(p -> authorized.addAll(pubPks.get(new ResourceRefHash(p))));
    NodeAccessControl.get()
        .filterAuthorizedByUser(ResourceRefHash.toNodePks(nodePks.keySet()), userId,
            AccessControlContext.init().onOperationsOf(operations))
        .forEach(p -> authorized.addAll(nodePks.get(new ResourceRefHash(p))));
    ComponentAccessControl.get().filterAuthorizedByUser(instanceIds.keySet(), userId,
            AccessControlContext.init().onOperationsOf(operations))
        .forEach(p -> authorized.addAll(instanceIds.get(p)));
    return resources.stream().filter(r -> authorized.contains(converter.apply(r)));
  }

  private boolean isHandledKmeliaResourceType(String objectType) {
    return objectType != null && (PUBLICATION_TYPE.equalsIgnoreCase(objectType)
        || objectType.startsWith(ATTACHMENT_TYPE) || objectType.startsWith(VERSION_TYPE));
  }

  /**
   * Hash to use as key in Map. This class ensures the equality and hash computation is done on
   * both the local identifier of a resource and on the identifier of the component instance the
   * resource belongs to. Indeed, in the general case, a contribution is uniquely identified by
   * its local id and by the component instance it belongs to, but some contributions can be
   * located in several component instances and in such a case they are uniquely identifier by
   * their local id (that is also, in this case, their global id).
   */
  private static class ResourceRefHash implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String localId;
    private final String instanceId;

    public static List<PublicationPK> toPublicationPKs(Collection<ResourceRefHash> hashes) {
      return hashes.stream().map(ResourceRefHash::toPublicationPK).collect(Collectors.toList());
    }

    public static List<NodePK> toNodePks(Collection<ResourceRefHash> hashes) {
      return hashes.stream().map(ResourceRefHash::toNodePK).collect(Collectors.toList());
    }

    public ResourceRefHash(String localId, String instanceId) {
      this.localId = localId;
      this.instanceId = instanceId;
    }

    public ResourceRefHash(ResourceReference resourceRef) {
      this.localId = resourceRef.getLocalId();
      this.instanceId = resourceRef.getInstanceId();
    }

    public PublicationPK toPublicationPK() {
      return new PublicationPK(localId, instanceId);
    }

    public NodePK toNodePK() {
      return new NodePK(localId, instanceId);
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ResourceRefHash that = (ResourceRefHash) o;
      return Objects.equals(localId, that.localId) && Objects.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(localId, instanceId);
    }
  }
}
