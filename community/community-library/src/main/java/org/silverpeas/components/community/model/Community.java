/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community.model;

import org.silverpeas.components.community.repository.CommunityRepository;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;

/**
 * It represents the business contribution handled in the Silverpeas component. Its persistence is
 * managed by the {@link org.silverpeas.components.community.repository.CommunityRepository} JPA repository.
 *
 * TODO by default the persistence entity and the contribution are merged into this single business
 * object. Nevertheless, according to the context, it would be more pertinent to modelize the
 * business entity into a Contribution object whose the state is itself represented by a persistent
 * entity (delegation pattern).
 */
@Entity
@Table(name = "SC_Community")
@NamedQuery(
    name = "CommunityByComponentInstanceId",
    query = "select c from Community c where c.componentInstanceId = :componentInstanceId " +
            "order by c.componentInstanceId, c.id")
public class Community
    extends SilverpeasJpaEntity<Community, UuidIdentifier>
    implements Contribution {

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  /**
   *  Constructs a new empty Community instance.
   */
  protected Community() {
    // this constructor is for the persistence engine.
  }

  /**
   * Constructs a new Community instance that belongs to the specified component instance.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  public Community(final String componentInstanceId) {
    this.componentInstanceId = componentInstanceId;
  }

  /**
   * Gets a Community by its identifier.
   * @param id the unique identifier of a Community contribution.
   * @return the Community instance or null if it does not exist.
   */
  public static Community getById(final String id) {
    CommunityRepository repository = CommunityRepository.get();
    return repository.getById(id);
  }

  /**
   * Gets all Community belonging to the specified component instance. If the component
   * instance doesn't exist or if no Community instances were created for this component
   * instance, then an empty list is returned.
   * @param instanceId the unique identifier of a component instance.
   * @return a list of Community instances or an empty list if there is no
   * Community belonging to the given component instance.
   */
  public static List<Community> getAllByComponentInstanceId(final String instanceId) {
    CommunityRepository repository = CommunityRepository.get();
    return repository.getByComponentInstanceId(instanceId);
  }

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(componentInstanceId, getId(), getContributionType());
  }

  /**
   * Saves this contribution state into the persistence context. If the contribution doesn't
   * exist yet its state is then persisted, otherwise its state is updated in the persistence
   * context.
   * @return the saved Community.
   */
  public Community save() {
    return Transaction.performInOne(() -> {
      CommunityRepository repository = CommunityRepository.get();
      return repository.save(this);
    });
  }

  /**
   * Deletes this contribution in the persistence context.
   */
  public void delete() {
    Transaction.performInOne(() -> {
      CommunityRepository repository = CommunityRepository.get();
      repository.delete(this);
      return null;
    });
  }
}