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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Created on 13 avr. 2005
 *
 */
package org.silverpeas.components.whitepages;

import org.silverpeas.components.whitepages.control.CardManager;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.components.whitepages.record.UserTemplate;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.web.index.components.ComponentIndexation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;

/**
 * @author neysseri
 */
@Technical
@Service
@Singleton
@Named("whitePages" + ComponentIndexation.QUALIFIER_SUFFIX)
public class WhitePagesIndexer implements ComponentIndexation {

  @Inject
  private CardManager cardManager;
  @Inject
  private PublicationTemplateManager templateManager;
  @Inject
  private Administration admin;

  @Override
  public void index(SilverpeasComponentInstance componentInst)
      throws org.silverpeas.core.SilverpeasException {
    Collection<Card> visibleCards;
    try {
      visibleCards = enrichWithUserRecordsAndCardRecords(componentInst.getId(),
          cardManager.getVisibleCards(componentInst.getId()));
    } catch (WhitePagesException e) {
      throw new org.silverpeas.core.SilverpeasException(e);
    }
    for (Card card : visibleCards) {
      cardManager.indexCard(card);
    }
  }

  private Collection<Card> enrichWithUserRecordsAndCardRecords(String componentId,
      Collection<Card> cards) throws WhitePagesException {
    List<Card> listCards = new ArrayList<>();
    try {
      if (cards != null && !cards.isEmpty()) {
        PublicationTemplate cardTemplate = getCardTemplateFor(componentId);
        UserTemplate userTemplate = getUserTemplateFor(componentId);
        for (Card card : cards) {
          String idCard = card.getPK().getId();
          if (userTemplate.getRecord(card.getUserId()).getUserDetail() == null) {
            // the user doesn't exist anymore
            deleteCard(cardTemplate, idCard);
          } else {
            card.writeUserRecord(userTemplate.getRecord(card.getUserId()));
            DataRecord cardRecord = cardTemplate.getRecordSet().getRecord(idCard);
            if (cardRecord == null) {
              cardRecord = cardTemplate.getRecordSet().getEmptyRecord();
            }
            card.writeCardRecord(cardRecord);
            listCards.add(card);
          }
        }
      }
    } catch (PublicationTemplateException | FormException e) {
      throw new WhitePagesException(e);
    }
    return listCards;
  }

  private PublicationTemplate getCardTemplateFor(String componentId)
      throws PublicationTemplateException {
    String cardTemplateFileName = admin.getComponentParameterValue(componentId, "cardTemplate");
    return templateManager.getPublicationTemplate(componentId, cardTemplateFileName);
  }

  private UserTemplate getUserTemplateFor(String componentId) {
    String userTemplateFileName =
        admin.getComponentParameterValue(componentId, "cardTemplate").replace('\\', '/');
    SettingBundle templateSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.whitePages.settings.template");
    String templateDir = templateSettings.getString("templateDir").replace('\\', '/');
    return new UserTemplate(templateDir + "/" + userTemplateFileName, null);
  }

  private void deleteCard(PublicationTemplate cardTemplate, String cardId)
      throws PublicationTemplateException, FormException, WhitePagesException {
    DataRecord data = cardTemplate.getRecordSet().getRecord(cardId);
    cardTemplate.getRecordSet().delete(data.getId());
    cardManager.delete(Collections.singletonList(cardId));
  }
}