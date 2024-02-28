/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community.model;

import org.silverpeas.components.community.repository.CommunityMembershipRepository;
import org.silverpeas.core.admin.BaseRightProfile;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * Membership of a given user in a community of users. Memberships are managed by the
 * {@link CommunityOfUsers} to which they are related and they are provided by the
 * {@link CommunityMembershipsProvider} which ensures the synchronization between the memberships to
 * a community and the users playing a role in the community space.
 * <p>
 * All along the life of his membership, the status of his membership in the community can change.
 * When a community requires a validation step for memberships requests, the user asking such a
 * thing has his membership created with a pending status. Otherwise, his membership is committed
 * automatically. Once his membership committed, a user becomes then a member of the community of
 * users. This means he has rights to access the content of the community space, which is a
 * collaborative space with a community of users. To become a member of a community for a space, a
 * user has to ask to join this community to the administrators. As a member of a community of a
 * space, the user can navigate within the space's tree according to his access rights. A user is
 * said to be a member of a given community space if and only if he plays a role in this space.
 * </p>
 *
 * @author mmoquillon
 */
@Entity
@Table(name = "SC_Community_Membership")
@NamedQuery(name = "byUserIdAndByCommunity",
    query = "select m from CommunityMembership m where m.userId = :userId and m.community = " +
        ":community and (m.status = org.silverpeas.components.community.model.MembershipStatus" +
        ".COMMITTED or m.status = org.silverpeas.components.community.model.MembershipStatus" +
        ".PENDING)")
@NamedQuery(name = "allNonRemoved",
    query =
        "select m from CommunityMembership m where m.community = :community and m.status <> org" +
            ".silverpeas.components.community.model.MembershipStatus.REMOVED order by m" +
            ".lastUpdateDate desc")
public class CommunityMembership extends SilverpeasJpaEntity<CommunityMembership, UuidIdentifier> {

  @OneToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "community", referencedColumnName = "id")
  @NotNull
  private CommunityOfUsers community;
  @Column(nullable = false)
  @NotNull
  private int userId;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull
  private MembershipStatus status;
  private Instant joiningDate;

  private transient User user;

  /**
   * Sets the specified user as a member of the specified community and gets his resulting
   * membership. The user isn't actually a member of the given community. For doing, invoke the
   * {@link CommunityOfUsers#addAsMember(User, SilverpeasRole)} method. This method is to be used by
   * the {@link CommunityOfUsers} instances.
   *
   * @param user the user to get as a member.
   * @param community the community for which the user has to be a member.
   * @return the membership of the given user to the specified community.
   */
  static CommunityMembership asMember(final User user, final CommunityOfUsers community) {
    CommunityMembership member = new CommunityMembership();
    member.user = user;
    member.userId = Integer.parseInt(user.getId());
    member.community = community;
    member.status = MembershipStatus.COMMITTED;
    return member;
  }

  /**
   * Constructs an empty member of nothing. To be used by the persistence engine when fetching
   * members objects from the database.
   */
  protected CommunityMembership() {
    // for JPA
  }

  /**
   * Gets the community to which this membership belongs.
   *
   * @return the community of this user.
   */
  public @Nonnull CommunityOfUsers getCommunity() {
    return community;
  }

  /**
   * Gets the user related to this membership.
   *
   * @return the user related to this membership
   */
  public @Nonnull User getUser() {
    if (user == null) {
      user = User.getById(String.valueOf(userId));
    }
    return user;
  }

  /**
   * Gets the current status of this membership.
   *
   * @return the membership status.
   */
  public @Nonnull MembershipStatus getStatus() {
    return status;
  }

  /**
   * Gets the date at which the user has effectively joined the community. The date at which his
   * membership has been committed. If the membership of the user to the community hasn't yet been
   * committed, then null is returned.
   *
   * @return the date and time in UTC or null if this membership hasn't been committed.
   */
  public OffsetDateTime getJoiningDate() {
    return joiningDate == null ? null : OffsetDateTime.ofInstant(joiningDate, ZoneOffset.UTC);
  }

  /**
   * Gets the role the user related by this membership plays in the community space. In the case the
   * user plays several roles, only the highest one is returned.
   *
   * @return the (highest) role the user plays in the community space. If the user isn't more a
   * member of the community, then null is returned.
   */
  public SilverpeasRole getMemberRole() {
    var roles =
        community.getCommunitySpace().getAllSpaceProfilesOfUser(String.valueOf(userId)).stream()
            .map(BaseRightProfile::getName)
            .map(SilverpeasRole::fromString)
            .collect(Collectors.toSet());
    return roles.isEmpty() ? null : SilverpeasRole.getHighestFrom(roles);
  }

  /**
   * Sets the new specified status to this membership to the underlying community of users. A member
   * of a community is never deleted from the table of members in the data storage. When he's
   * removed from the community, his status in the data storage is just updated to
   * {@link MembershipStatus#REMOVED}. Only the {@link CommunityOfUsers} instances should use this
   * method as their goal is also to control the membership of a user to them.
   *
   * @param newStatus the new membership status.
   */
  void setStatus(final MembershipStatus newStatus) {
    this.status = newStatus;
  }

  /**
   * Saves the state of this membership into the table of memberships of the underlying community of
   * users. If such membership already exists in the table, and it hasn't been removed, then its
   * state is then just updated. In the case this member related by this membership has been removed
   * from the community, an {@link IllegalStateException} is thrown. To delete the membership of a
   * user in the community, please use instead the {@link CommunityMembership#delete()} method. This
   * method is to be used by the {@link CommunityOfUsers} and the
   * {@link CommunityMembershipsProvider} instances.
   *
   * @throws IllegalStateException if the status of this membership is
   * {@link MembershipStatus#REMOVED} as a removed membership cannot be anymore modified.
   */
  void save() {
    if (status == MembershipStatus.REMOVED) {
      throw new IllegalStateException(
          "The user isn't anymore member of the community and as such its state cannot be updated");
    }
    Transaction.performInOne(() -> {
      if (status == MembershipStatus.COMMITTED && joiningDate == null) {
        joiningDate = OffsetDateTime.now(ZoneOffset.UTC).toInstant();
      }
      CommunityMembershipRepository.get().save(this);
      return null;
    });
  }

  /**
   * Deletes the membership of the user in the underlying community of users. The membership of the
   * user won't be effectively deleted in the table of memberships of the underlying community of
   * users; instead his membership status will pass to {@link MembershipStatus#REMOVED} before being
   * saved as such in the table of memberships. This method is to be used by the
   * {@link CommunityOfUsers} and the {@link CommunityMembershipsProvider} instances.
   *
   * @throws IllegalStateException if the status of this membership is
   * {@link MembershipStatus#PENDING} as a pending membership cannot be removed but only refused or
   * accepted.
   */
  void delete() {
    if (status == MembershipStatus.PENDING) {
      throw new IllegalStateException(
          "A pending membership of a user in the community cannot be removed! It can be only " +
              "refused or validated");
    }
    Transaction.performInOne(() -> {
      status = MembershipStatus.REMOVED;
      CommunityMembershipRepository.get().save(this);
      return null;
    });
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
