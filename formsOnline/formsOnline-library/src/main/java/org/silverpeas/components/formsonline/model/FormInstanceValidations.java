/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author silveryocha
 */
public class FormInstanceValidations extends ArrayList<FormInstanceValidation> {
  private static final long serialVersionUID = 17226772888377972L;

  private final transient Map<FormInstanceValidationType, Optional<FormInstanceValidation>> byType = new ConcurrentHashMap<>();

  public FormInstanceValidations() {
    super(3);
    resetCache();
  }

  /**
   * Gets the hierarchical validation.
   * @return an optional {@link FormInstanceValidation} instance.
   */
  public Optional<FormInstanceValidation> getHierarchicalValidation() {
    return getValidationOfType(FormInstanceValidationType.HIERARCHICAL);
  }

  /**
   * Gets the intermediate validation.
   * @return an optional {@link FormInstanceValidation} instance.
   */
  public Optional<FormInstanceValidation> getIntermediateValidation() {
    return getValidationOfType(FormInstanceValidationType.INTERMEDIATE);
  }

  /**
   * Gets the final validation.
   * @return an optional {@link FormInstanceValidation} instance.
   */
  public Optional<FormInstanceValidation> getFinalValidation() {
    return getValidationOfType(FormInstanceValidationType.FINAL);
  }

  /**
   * Gets the validation corresponding of the given type.
   * @return an optional {@link FormInstanceValidation} instance.
   */
  public Optional<FormInstanceValidation> getValidationOfType(
      final FormInstanceValidationType type) {
    return byType.computeIfAbsent(type,
        t -> stream().filter(v -> v.getValidationType() == type).findFirst());
  }

  public Optional<FormInstanceValidation> getLatestValidation() {
    return Optional.ofNullable(getFinalValidation()
        .orElseGet(() -> getIntermediateValidation()
            .orElseGet(() -> getHierarchicalValidation()
                .orElse(null))));
  }

  private void resetCache() {
    byType.clear();
  }

  @Override
  public FormInstanceValidation set(final int index, final FormInstanceValidation element) {
    resetCache();
    return super.set(index, element);
  }

  @Override
  public boolean add(final FormInstanceValidation formInstanceValidation) {
    resetCache();
    return super.add(formInstanceValidation);
  }

  @Override
  public void add(final int index, final FormInstanceValidation element) {
    resetCache();
    super.add(index, element);
  }

  @Override
  public FormInstanceValidation remove(final int index) {
    resetCache();
    return super.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    resetCache();
    return super.remove(o);
  }

  @Override
  public void clear() {
    resetCache();
    super.clear();
  }

  @Override
  public boolean addAll(final Collection<? extends FormInstanceValidation> c) {
    resetCache();
    return super.addAll(c);
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends FormInstanceValidation> c) {
    resetCache();
    return super.addAll(index, c);
  }

  @Override
  protected void removeRange(final int fromIndex, final int toIndex) {
    resetCache();
    super.removeRange(fromIndex, toIndex);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    resetCache();
    return super.removeAll(c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    resetCache();
    return super.retainAll(c);
  }

  @Override
  public List<FormInstanceValidation> subList(final int fromIndex, final int toIndex) {
    resetCache();
    return super.subList(fromIndex, toIndex);
  }

  @Override
  public boolean removeIf(final Predicate<? super FormInstanceValidation> filter) {
    resetCache();
    return super.removeIf(filter);
  }

  @Override
  public void replaceAll(final UnaryOperator<FormInstanceValidation> operator) {
    resetCache();
    super.replaceAll(operator);
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
