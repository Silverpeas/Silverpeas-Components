package org.silverpeas.components.formsonline.model;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.silverpeas.components.formsonline.model.FormInstance.*;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.HIERARCHICAL;

/**
 * @author Nicolas Eysseric
 */
public class RequestsByStatus {

  static final List<MergeRuleByStates>
      MERGING_RULES_BY_STATES = asList(
          new MergeRuleByStates(singletonList(STATE_DRAFT), (f, t) -> {}, RequestsByStatus::addDraft),
          new MergeRuleByStates(singletonList(STATE_REFUSED), (f, t) -> {}, RequestsByStatus::addDenied),
          new MergeRuleByStates(singletonList(STATE_VALIDATED), (f, t) -> {}, RequestsByStatus::addValidated),
          new MergeRuleByStates(singletonList(STATE_ARCHIVED), (f, t) -> {}, RequestsByStatus::addArchived),
          new MergeRuleByStates(singletonList(STATE_CANCELED), canceledCriteriaConfigurer(), RequestsByStatus::addCanceled),
          new MergeRuleByStates(asList(STATE_UNREAD, STATE_READ), toValidateCriteriaConfigurer(), RequestsByStatus::addToValidate),
          new MergeRuleByStates(singletonList(STATE_READ), stillValidationNeededCriteriaConfigurer(), RequestsByStatus::addStillNeedValidation));

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
  private SilverpeasList<FormInstance> stillNeedValidationList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> validatedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> deniedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> archivedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> canceledList = new SilverpeasArrayList<>();

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

  private void addStillNeedValidation(final SilverpeasList<FormInstance> formInstances) {
    stillNeedValidationList = merge(formInstances, stillNeedValidationList);
  }

  private void addCanceled(final SilverpeasList<FormInstance> formInstances) {
    canceledList = merge(formInstances, canceledList);
  }

  public SilverpeasList<FormInstance> getDraft() {
    return draftList;
  }

  public SilverpeasList<FormInstance> getToValidate() {
    return toValidateList;
  }

  public SilverpeasList<FormInstance> getStillNeedValidation() {
    return stillNeedValidationList;
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

  public SilverpeasList<FormInstance> getCanceled() {
    return canceledList;
  }

  public boolean isEmpty() {
    return getDraft().isEmpty() && getValidated().isEmpty() && getToValidate().isEmpty() &&
        getStillNeedValidation().isEmpty() && getDenied().isEmpty() && getArchived().isEmpty() &&
        getCanceled().isEmpty();
  }

  public SilverpeasList<FormInstance> getAll() {
    return merge(getDraft(), getToValidate(), getStillNeedValidation(), getValidated(), getDenied(),
        getArchived(), getCanceled());
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

  static BiConsumer<Pair<Set<FormInstanceValidationType>,
        Set<FormInstanceValidationType>>, RequestValidationCriteria> toValidateCriteriaConfigurer() {
    return (f, t) -> {
      t.andOnlyToValidateByValidator();
      final Set<FormInstanceValidationType> possibleFormValidationTypes = f.getLeft();
      final Set<FormInstanceValidationType> possibleValidatorValidationTypes = f.getRight().stream()
          .filter(possibleFormValidationTypes::contains)
          .collect(Collectors.toSet());
      setLastValidationTypeCriteria(possibleFormValidationTypes, possibleValidatorValidationTypes, t);
      final boolean isHierarchicalFormValidation = possibleFormValidationTypes.contains(HIERARCHICAL);
      if (isHierarchicalFormValidation) {
        if (possibleValidatorValidationTypes.contains(HIERARCHICAL)) {
          t.orValidatorIsHierarchicalOne();
        }
      } else if (possibleValidatorValidationTypes.stream()
          .filter(v -> v != HIERARCHICAL)
          .anyMatch(v -> possibleFormValidationTypes.stream().findFirst().filter(p -> p == v).isPresent())) {
        t.orNoValidator();
      }
    };
  }

  static BiConsumer<Pair<Set<FormInstanceValidationType>,
      Set<FormInstanceValidationType>>, RequestValidationCriteria> canceledCriteriaConfigurer() {
    return (f, t) -> t.orNoValidator();
  }

  static BiConsumer<Pair<Set<FormInstanceValidationType>,
        Set<FormInstanceValidationType>>, RequestValidationCriteria> stillValidationNeededCriteriaConfigurer() {
    return (f, t) -> {
      t.andStillNeedValidation();
      final Set<FormInstanceValidationType> possibleFormValidationTypes = f.getLeft();
      setLastValidationTypeCriteria(possibleFormValidationTypes, possibleFormValidationTypes, t);
    };
  }

  private static void setLastValidationTypeCriteria(
      final Set<FormInstanceValidationType> possibleFormValidationTypes,
      final Set<FormInstanceValidationType> possibleValidatorValidationTypes,
      final RequestValidationCriteria criteria) {
    final Set<FormInstanceValidationType> lastValidationFilter = new TreeSet<>();
    possibleValidatorValidationTypes.stream()
        .filter(v -> v != HIERARCHICAL)
        .forEach(v -> Stream.of(FormInstanceValidationType.values())
            .sorted(Comparator.reverseOrder())
            .filter(l -> l.ordinal() < v.ordinal())
            .filter(possibleFormValidationTypes::contains)
            .findFirst()
            .ifPresent(lastValidationFilter::add));
    criteria.orLastValidationType(lastValidationFilter);
  }

  public static class MergeRuleByStates {
    private final List<Integer> states;
    private final BiConsumer<Pair<Set<FormInstanceValidationType>, Set<FormInstanceValidationType>>, RequestValidationCriteria> validationCriteriaConfigurer;
    private final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merger;

    public MergeRuleByStates(final List<Integer> states,
        final BiConsumer<Pair<Set<FormInstanceValidationType>, Set<FormInstanceValidationType>>, RequestValidationCriteria> validationCriteriaConfigurer,
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merger) {
      this.states = states;
      this.validationCriteriaConfigurer = validationCriteriaConfigurer;
      this.merger = merger;
    }

    public List<Integer> getStates() {
      return states;
    }

    public BiConsumer<Pair<Set<FormInstanceValidationType>,
        Set<FormInstanceValidationType>>, RequestValidationCriteria> getValidationCriteriaConfigurer() {
      return validationCriteriaConfigurer;
    }

    public BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> getMerger() {
      return merger;
    }
  }
}