/*
 * Copyright (C) 2000 - 2015 Silverpeas
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

/*
 * Created on 13 avr. 2005
 *
 */
package com.silverpeas.whitePages;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.whitePages.control.CardManager;
import com.silverpeas.whitePages.model.Card;
import com.silverpeas.whitePages.record.UserTemplate;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexation;
import com.stratelia.webactiv.beans.admin.Administration;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author neysseri
 */
@Singleton
@Named("WhitePagesComponentIndexation")
public class WhitePagesIndexer implements ComponentIndexation {

  @Inject
  private CardManager cardManager;
  @Inject
  private PublicationTemplateManager templateManager;
  @Inject
  private Administration admin;

  @Override
  public void index(ComponentInst componentInst) throws Exception {
    Collection<Card> visibleCards = enrichWithUserRecordsAndCardRecords(componentInst.getId(),
        cardManager.getVisibleCards(componentInst.getId()));
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
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesIndexer.enrichWithUserRecordsAndCardRecords",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesIndexer.enrichWithUserRecordsAndCardRecords",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
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
    ResourceLocator templateSettings =
        new ResourceLocator("org.silverpeas.whitePages.settings.template", "");
    UserDetail currentUser = UserDetail.getCurrentRequester();
    String language = currentUser.getUserPreferences().getLanguage();
    String templateDir = templateSettings.getString("templateDir").replace('\\', '/');
    return new UserTemplate(templateDir + "/" + userTemplateFileName, language);
  }

  private void deleteCard(PublicationTemplate cardTemplate, String cardId)
      throws PublicationTemplateException, FormException, WhitePagesException {
    DataRecord data = cardTemplate.getRecordSet().getRecord(cardId);
    cardTemplate.getRecordSet().delete(data);
    cardManager.delete(Arrays.asList(cardId));
  }
}