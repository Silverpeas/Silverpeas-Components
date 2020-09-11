package org.silverpeas.components.formsonline.model;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.naturalOrder;
import static org.silverpeas.components.formsonline.model.FormInstance.*;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.HIERARCHICAL;

/**
 * @author Nicolas Eysseric
 */
public class RequestsByStatus {

  private static final Comparator<FormInstance> FORM_INSTANCE_COMPARATOR = (a, b) -> {
    int c = b.getCreationDate().compareTo(a.getCreationDate());
    if (c == 0) {
      c = b.getIdAsInt() - a.getIdAsInt();
    }
    return c;
  };

  static final BiConsumer<Pair<Set<FormInstanceValidationType>,
      Set<FormInstanceValidationType>>, RequestValidationCriteria> toValidateCriteriaConfigurer =
      (f, t) -> {
    t.andAvoidValidatedByValidator();
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

  static final BiConsumer<Pair<Set<FormInstanceValidationType>,
      Set<FormInstanceValidationType>>, RequestValidationCriteria> concernedByValidationCriteriaConfigurer = (f, t) -> {
    final Set<FormInstanceValidationType> possibleFormValidationTypes = f.getLeft();
    final boolean isLastValidatorCase = isLastValidatorCase(possibleFormValidationTypes, f.getRight());
    if (isLastValidatorCase) {
      toValidateCriteriaConfigurer.accept(f, t);
      t.invert();
    } else {
      t.andStillNeedValidation();
      setLastValidationTypeCriteria(possibleFormValidationTypes, possibleFormValidationTypes, t);
    }
  };

  static final BiConsumer<Pair<Set<FormInstanceValidationType>,
      Set<FormInstanceValidationType>>, RequestValidationCriteria> skipValidationCriteriaIfLastValidatorConfigurer = (f, t) -> {
    if (isLastValidatorCase(f.getLeft(), f.getRight())) {
      t.skipValidationFiltering();
    }
  };

  static final BiConsumer<Pair<Set<FormInstanceValidationType>,
      Set<FormInstanceValidationType>>, RequestValidationCriteria> canceledCriteriaConfigurer =
      (f, t) -> {
    skipValidationCriteriaIfLastValidatorConfigurer.accept(f, t);
    if (!t.isSkipValidationFiltering()) {
      t.orNoValidator();
    }
  };

  static final List<MergeRuleByStates> MERGING_RULES_BY_STATES = asList(
    new MergeRuleByStates(singletonList(STATE_DRAFT), RequestsByStatus::addDraft),
    new MergeRuleByStates(singletonList(STATE_REFUSED), RequestsByStatus::addDenied),
    new MergeRuleByStates(singletonList(STATE_VALIDATED), RequestsByStatus::addValidated),
    new MergeRuleByStates(singletonList(STATE_ARCHIVED), RequestsByStatus::addArchived),
    new MergeRuleByStates(singletonList(STATE_CANCELED), RequestsByStatus::addCanceled),
    new MergeRuleByStates(asList(STATE_UNREAD, STATE_READ), RequestsByStatus::addToValidate));

  static final List<ValidationMergeRuleByStates> VALIDATION_MERGING_RULES_BY_STATES = asList(
    new ValidationMergeRuleByStates(singletonList(STATE_DRAFT), skipValidationCriteriaIfLastValidatorConfigurer, RequestsByStatus::addDraft),
    new ValidationMergeRuleByStates(singletonList(STATE_REFUSED), skipValidationCriteriaIfLastValidatorConfigurer, RequestsByStatus::addDenied),
    new ValidationMergeRuleByStates(singletonList(STATE_VALIDATED), skipValidationCriteriaIfLastValidatorConfigurer, RequestsByStatus::addValidated),
    new ValidationMergeRuleByStates(singletonList(STATE_ARCHIVED), skipValidationCriteriaIfLastValidatorConfigurer, RequestsByStatus::addArchived),
    new ValidationMergeRuleByStates(singletonList(STATE_CANCELED), canceledCriteriaConfigurer, RequestsByStatus::addCanceled),
    new ValidationMergeRuleByStates(asList(STATE_UNREAD, STATE_READ), toValidateCriteriaConfigurer, RequestsByStatus::addToValidate),
    new ValidationMergeRuleByStates(asList(STATE_UNREAD, STATE_READ), concernedByValidationCriteriaConfigurer, RequestsByStatus::addConcernedByValidation));

  private final PaginationPage paginationPage;
  private SilverpeasList<FormInstance> draftList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> toValidateList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> concernedByValidationList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> validatedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> deniedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> archivedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> canceledList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> all = null;

  RequestsByStatus(final PaginationPage paginationPage) {
    this.paginationPage = paginationPage;
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

  private static boolean isLastValidatorCase(
      final Set<FormInstanceValidationType> possibleFormValidationTypes,
      final Set<FormInstanceValidationType> possibleValidatorValidationTypes) {
    return possibleValidatorValidationTypes.stream()
        .filter(possibleFormValidationTypes::contains)
        .max(naturalOrder())
        .filter(v -> possibleFormValidationTypes.stream().max(naturalOrder()).orElse(null) == v)
        .isPresent();
  }

  /**
   * Gets the possible request validations from given requests.
   * <p>
   *   BE CAREFUL of that following methods MUST have been called before using this method:
   *   <ul>
   *     <li>{@link FormDetail#setIntermediateReceiversAsUsers(List)}</li>
   *     <li>{@link FormDetail#setIntermediateReceiversAsGroups(List)}</li>
   *     <li>{@link FormDetail#setReceiversAsUsers(List)}</li>
   *     <li>{@link FormDetail#setReceiversAsGroups(List)}</li>
   *   </ul>
   * </p>
   * @return a set of possible request validations sorted as the {@link FormInstanceValidationType} enum.
   */
  public static Set<FormInstanceValidationType> possibleRequestValidationsFrom(
      final Collection<FormInstance> requests) {
    return requests.stream()
        .map(FormInstance::getForm)
        .distinct()
        .flatMap(f -> f.getPossibleRequestValidations().keySet().stream())
        .collect(Collectors.toCollection(TreeSet::new));
  }

  private void addDraft(final SilverpeasList<FormInstance> formInstances) {
    resetAll();
    draftList = merge(formInstances, draftList);
  }

  private void addArchived(final SilverpeasList<FormInstance> formInstances) {
    resetAll();
    archivedList = merge(formInstances, archivedList);
  }

  private void addDenied(final SilverpeasList<FormInstance> formInstances) {
    resetAll();
    deniedList = merge(formInstances, deniedList);
  }

  private void addValidated(final SilverpeasList<FormInstance> formInstances) {
    resetAll();
    validatedList = merge(formInstances, validatedList);
  }

  private void addToValidate(final SilverpeasList<FormInstance> formInstances) {
    resetAll();
    toValidateList = merge(formInstances, toValidateList);
  }

  private void addConcernedByValidation(final SilverpeasList<FormInstance> formInstances) {
    resetAll();
    concernedByValidationList = merge(formInstances, concernedByValidationList);
  }

  private void addCanceled(final SilverpeasList<FormInstance> formInstances) {
    resetAll();
    canceledList = merge(formInstances, canceledList);
  }

  public SilverpeasList<FormInstance> getDraft() {
    return draftList;
  }

  public SilverpeasList<FormInstance> getToValidate() {
    return toValidateList;
  }

  public SilverpeasList<FormInstance> getConcernedByValidation() {
    return concernedByValidationList;
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
    return getAll().isEmpty();
  }

  public SilverpeasList<FormInstance> getAll() {
    if (all == null) {
      all = merge(getDraft(), getToValidate(), getConcernedByValidation(), getValidated(),
          getDenied(), getArchived(), getCanceled());
    }
    return all;
  }

  private void resetAll() {
    all = null;
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
    private final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merger;

    public MergeRuleByStates(final List<Integer> states,
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merger) {
      this.states = states;
      this.merger = merger;
    }

    public List<Integer> getStates() {
      return states;
    }

    public BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> getMerger() {
      return merger;
    }
  }

  public static class ValidationMergeRuleByStates extends MergeRuleByStates {
    private final BiConsumer<Pair<Set<FormInstanceValidationType>, Set<FormInstanceValidationType>>, RequestValidationCriteria> validationCriteriaConfigurer;

    public ValidationMergeRuleByStates(final List<Integer> states,
        final BiConsumer<Pair<Set<FormInstanceValidationType>, Set<FormInstanceValidationType>>, RequestValidationCriteria> validationCriteriaConfigurer,
        final BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>> merger) {
      super(states, merger);
      this.validationCriteriaConfigurer = validationCriteriaConfigurer;
    }

    public BiConsumer<Pair<Set<FormInstanceValidationType>,
        Set<FormInstanceValidationType>>, RequestValidationCriteria> getValidationCriteriaConfigurer() {
      return validationCriteriaConfigurer;
    }
  }
}