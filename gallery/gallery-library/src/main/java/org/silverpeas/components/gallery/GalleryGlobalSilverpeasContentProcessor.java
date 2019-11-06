/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery;

import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractContentInterface.ContributionWrapper;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.util.Pair;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.silverpeas.components.gallery.GalleryComponentSettings.COMPONENT_NAME;
import static org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContentProcessor.Constants.PROCESSOR_NAME_SUFFIX;

@Named(COMPONENT_NAME + PROCESSOR_NAME_SUFFIX)
public class GalleryGlobalSilverpeasContentProcessor extends AbstractGlobalSilverContentProcessor {

  @Override
  public String relatedToComponent() {
    return COMPONENT_NAME;
  }

  @Override
  public Stream<GlobalSilverContent> asGlobalSilverContent(List<SilverContentInterface> silverContents) {
    final Map<String, Pair<String, MediaType>> mediaThumbnails = silverContents.stream()
        .map(c -> (Media) ((ContributionWrapper) c).getWrappedInstance())
        .collect(toMap(Media::getId,
                       m -> Pair.of(m.getApplicationThumbnailUrl(MediaResolution.TINY), m.getType())));
    return super.asGlobalSilverContent(silverContents).peek(g -> {
      final Pair<String, MediaType> mediaThumbnail = mediaThumbnails.get(g.getId());
      g.setThumbnailURL(mediaThumbnail.getFirst());
      g.setType(mediaThumbnail.getSecond().getName());
    });
  }

}