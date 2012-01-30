/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.resourcemanager.services;

import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Service
@Transactional
public class CategoryService {
  @Inject
  CategoryRepository repository;


  public String createCategory(Category category) {
    Category savedCategory = repository.saveAndFlush(category);
    return savedCategory.getId();
  }

  public void updateCategory(Category category) {
    repository.saveAndFlush(category);
  }

  public List<Category> getCategories(String instanceId) {
    return repository.findCategoriesByInstanceId(instanceId);
  }

  public Category getCategory(String id) {
    return repository.findOne(Integer.parseInt(id));
  }

  public void deleteCategory(String id) {
    repository.delete(Integer.parseInt(id));
  }
}
