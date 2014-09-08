package org.silverpeas.components.quickinfo.repository;

import java.util.List;

import javax.inject.Named;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.jpa.NamedParameters;
import org.silverpeas.persistence.repository.jpa.SilverpeasJpaEntityManager;

@Named
public class NewsRepository extends SilverpeasJpaEntityManager<News, UuidIdentifier> {
  
  public List<News> getByComponentId(String componentId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("componentInstanceId", componentId);
    return findByNamedQuery("newsFromComponentInstance", parameters);
  }
  
  public News getByForeignId(String foreignId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("foreignId", foreignId);
    return findByNamedQuery("newsByForeignId", parameters).get(0);
  }

}
