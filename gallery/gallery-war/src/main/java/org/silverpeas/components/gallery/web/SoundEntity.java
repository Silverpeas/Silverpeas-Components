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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.web;

import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.model.Sound;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SoundEntity extends AbstractMediaEntity<SoundEntity> {
  private static final long serialVersionUID = 3613940673764513915L;

  @XmlElement(defaultValue = "")
  private URI embedUrl;

  /**
   * Creates a new sound entity from the specified sound.
   * @param sound the sound from which the entity is created.
   * @return the entity representing the specified sound.
   */
  public static SoundEntity createFrom(final Sound sound) {
    return new SoundEntity(sound);
  }

  public URI getEmbedUrl() {
    return embedUrl;
  }

  /**
   * Default hidden constructor.
   */
  private SoundEntity(final Sound sound) {
    super(sound);
    embedUrl = URI.create(sound.getApplicationEmbedUrl(MediaResolution.PREVIEW));
  }

  @SuppressWarnings("UnusedDeclaration")
  protected SoundEntity() {
    super();
  }
}
