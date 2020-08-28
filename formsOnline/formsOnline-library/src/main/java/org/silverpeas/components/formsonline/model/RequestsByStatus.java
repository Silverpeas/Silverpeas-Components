package org.silverpeas.components.formsonline.model;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.components.formsonline.model.DefaultFormsOnlineService.HierarchicalValidatorCacheManager;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.silverpeas.components.formsonline.model.FormInstance.*;

/**
 * @author Nicolas Eysseric
 */
public class RequestsByStatus {

  static final List<MergeRuleByStates>
      MERGING_RULES_BY_STATES = asList(
          new MergeRuleByStates(singletonList(STATE_DRAFT), s -> emptySet(), RequestsByStatus::addDraft),
          new MergeRuleByStates(singletonList(STATE_REFUSED), s -> emptySet(), RequestsByStatus::addDenied),
          new MergeRuleByStates(singletonList(STATE_VALIDATED), s -> emptySet(), RequestsByStatus::addValidated),
          new MergeRuleByStates(singletonList(STATE_ARCHIVED), s -> emptySet(), RequestsByStatus::addArchived),
          new MergeRuleByStates(asList(STATE_UNREAD, STATE_READ), RequestsByStatus::getDomainUsersManagedBy, RequestsByStatus::addToValidate));

  private static final Comparator<FormInstance> FORM_INSTANCE_COMPARATOR = (a, b) -> {
    int c = b.getCreationDate().compareTo(a.getCreationDate());
    if (c == 0) {
      c = Integer.parseInt(b.getId()) - Integer.parseInt(a.getId());
    }
    return c;
  };
  private final PaginationPage paginationPage;

  private SilverpeasList<FormInstance> draftList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> toValidateList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> validatedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> deniedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> archivedList = new SilverpeasArrayList<>();

  RequestsByStatus(final PaginationPage paginationPage) {
    this.paginationPage = paginationPage;
  }

  private void addDraft(final SilverpeasList<FormInstance> formInstances) {
    draftList = merge(formInstances, draftList);
  }

  private void addArchived(final SilverpeasList<FormInstance> formInstances) {
    archivedList = merge(formInstances, archivedList);
  }

  private void addDenied(final SilverpeasList<FormInstance> formInstances) {
    deniedList = merge(formInstances, deniedList);
  }

  private void addValidated(final SilverpeasList<FormInstance> formInstances) {
    validatedList = merge(formInstances, validatedList);
  }

  private void addToValidate(final SilverpeasList<FormInstance> formInstances) {
    toValidateList = merge(formInstances, toValidateList);
  }

  public SilverpeasList<FormInstance> getDraft() {
    return draftList;
  }

  public SilverpeasList<FormInstance> getToValidate() {
    return toValidateList;
  }

  public SilverpeasList<FormInstance> getDenied() {
    return deniedList;
  }

  public SilverpeasList<FormInstance> getValidated() {
    return validatedList;
  }

  public SilverpeasList<FormInstance> getArchived() {
    return archivedList;
  }

  public boolean isEmpty() {
    return getDraft().isEmpty() && getValidated().isEmpty() && getToValidate().isEmpty() &&
        getDenied().isEmpty() && getArchived().isEmpty();
  }

  public SilverpeasList<FormInstance> getAll() {
    return merge(getDraft(), getToValidate(), getValidated(), getDenied(), getArchived());
  }

  /**
   * Merges the two given lists without modifying them into a new one.
   * @param lists the lists to merge.
   * @return the list which is the result of merge.
   */
  @SafeVarargs
  private final SilverpeasList<FormInstance> merge(final SilverpeasList<FormInstance>... lists) {
    int size = 0;
    int maxSize = 0;
    for (SilverpeasList<FormInstance> list : lists) {
      size += list.size();
      maxSize += list.originalListSize();
    }
    final List<FormInstance> merge = new ArrayList<>(size);
    for (SilverpeasList<FormInstance> list : lists) {
      merge.addAll(list);
    }
    Stream<FormInstance> resultStream = merge.stream().sorted(FORM_INSTANCE_COMPARATOR);
    if (paginationPage != null) {
      resultStream = resultStream.limit(paginationPage.getPageSize());
    }
    return PaginationList.from(resultStream.collect(Collectors.toList()), maxSize);
  }

  public static class MergeRuleByStates {
    private final List<Integer> states;
    private final Function<String, Set<String>> domainUsersManagedBy;
    private final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merger;

    public MergeRuleByStates(final List<Integer> states,
        final Function<String, Set<String>> domainUsersManagedBy,
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merger) {
      this.states = states;
      this.domainUsersManagedBy = domainUsersManagedBy;
      this.merger = merger;
    }

    public List<Integer> getStates() {
      return states;
    }

    public Set<String> getDomainUsersManagedBy(final FormDetail form, final String managerId) {
      if (!form.isHierarchicalValidation()) {
        return Collections.emptySet();
      }
      return domainUsersManagedBy.apply(managerId);
    }

    public BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> getMerger() {
      return merger;
    }
  }

  private static Set<String> getDomainUsersManagedBy(final String userId) {
    final String userDomainId = User.getById(userId).getDomainId();
    final User[] users = OrganizationController.get().getAllUsersInDomain(userDomainId);
    final Set<String> userIds = Stream.of(users).map(User::getId).collect(toSet());
    final HierarchicalValidatorCacheManager hvManager = new HierarchicalValidatorCacheManager();
    hvManager.cacheHierarchicalValidatorsOf(userIds);
    return userIds.stream()
        .map(u -> Pair.of(u, hvManager.getHierarchicalValidatorOf(u)))
        .filter(p -> userId.equals(p.getRight()))
        .map(Pair::getLeft)
        .collect(toSet());
  }
}