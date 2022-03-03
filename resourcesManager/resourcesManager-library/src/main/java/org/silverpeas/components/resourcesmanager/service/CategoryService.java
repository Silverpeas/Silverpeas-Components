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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager.service;

import org.silverpeas.components.resourcesmanager.model.Category;
import org.silverpeas.components.resourcesmanager.repository.CategoryRepository;
import org.silverpeas.core.annotation.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoryService {
  @Inject
  private CategoryRepository repository;

  public void createCategory(Category category) {
    repository.saveAndFlush(category);
  }

  public void updateCategory(Category category) {
    repository.saveAndFlush(category);
  }

  public List<Category> getCategories(String instanceId) {
    return repository.findCategoriesByInstanceId(instanceId);
  }

  public Category getCategory(Long id) {
    return repository.getById(Long.toString(id));
  }

  public void deleteCategory(Long id) {
    repository.deleteById(Long.toString(id));
  }
}
