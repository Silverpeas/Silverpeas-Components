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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.survey.web;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.security.html.HtmlSanitizer;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.web.rs.WebEntity;

import java.net.URI;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * This is a WEB entity representation of an {@link Answer} linked to a {@link Question}.
 * @author silveryocha
 */
public class AnswerEntity implements WebEntity {
  private static final long serialVersionUID = 8677864967906410516L;

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.survey.surveySettings");

  private String id;
  private String instanceId;
  private String questionId;
  private String label;
  private String imageUrl;
  private boolean opened;
  private int nbVoters;

  protected AnswerEntity() {
  }

  /**
   * Gets the {@link AnswerEntity} instance from given {@link Answer} one.
   * @param answer the answer model to get into WEB entity representation.
   * @return an {@link AnswerEntity} instance.
   */
  public static AnswerEntity asWebEntity(final Answer answer) {
    final AnswerEntity entity = new AnswerEntity();
    final AnswerPK pk = answer.getPK();
    final ResourceReference questionPK = answer.getQuestionPK();
    entity.id = pk.getId();
    entity.instanceId = questionPK.getComponentInstanceId();
    entity.questionId = questionPK.getLocalId();
    entity.label = HtmlSanitizer.get().sanitize(answer.getLabel());
    entity.nbVoters = answer.getNbVoters();
    entity.opened = answer.isOpened();
    entity.imageUrl = normalizeImageUrl(pk.getInstanceId(), answer.getImage()).orElse(null);
    return entity;
  }

  /**
   * Normalizes the image URL of an answer. This method does not take into parameters directly an
   * {@link Answer} instance in order to be used in context of survey creation. In a such
   * context, {@link Answer} data are not safe.
   * @param instanceId the identifier of the survey component instance.
   * @param imageUrl the URL of the image linked to an answer.
   * @return an optional normalized URL.
   */
  public static Optional<String> normalizeImageUrl(final String instanceId, final String imageUrl) {
    Optional<String> url = empty();
    if (isDefined(imageUrl)) {
      if (imageUrl.startsWith("/")) {
        url = of(imageUrl + "&Size=266x150");
      } else {
        url = of(FileServerUtils.getUrl(instanceId, imageUrl, imageUrl, "image/gif",
            settings.getString("imagesSubDirectory")));
      }
    }
    return url;
  }

  /**
   * Gets the identifier of the answer.
   * @return a string identifier.
   */
  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  /**
   * Gets the identifier of the survey component instance hosting the answer.
   * @return a string identifier.
   */
  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(final String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Gets the identifier of the question the answer is linked to.
   * @return a string identifier.
   */
  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(final String questionId) {
    this.questionId = questionId;
  }

  /**
   * Gets the label of the answer.
   * @return a string.
   */
  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  /**
   * Indicates if the answer is an opened one.
   * @return true if opened, false otherwise.
   */
  public boolean isOpened() {
    return opened;
  }

  public void setOpened(final boolean opened) {
    this.opened = opened;
  }

  /**
   * Gets the number of voters who choose the answer.
   * @return an integer.
   */
  public int getNbVoters() {
    return nbVoters;
  }

  public void setNbVoters(final int nbVoters) {
    this.nbVoters = nbVoters;
  }

  /**
   * Gets the URL of a linked image if any.
   * @return a string if any, null or empty otherwise.
   */
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Override
  public URI getURI() {
    return null;
  }
}
