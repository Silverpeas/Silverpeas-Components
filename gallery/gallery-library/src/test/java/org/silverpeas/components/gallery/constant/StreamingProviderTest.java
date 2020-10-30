/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.constant;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.components.gallery.constant.StreamingProvider.getOembedUrl;

@EnableSilverTestEnv
class StreamingProviderTest {

  @Test
  void testFrom() {
    assertThat(StreamingProvider.ALL_VALIDS, hasSize(StreamingProvider.values().length - 1));
    assertThat(StreamingProvider.from(null), is(StreamingProvider.unknown));
    assertThat(StreamingProvider.from(""), is(StreamingProvider.unknown));
    assertThat(StreamingProvider.from(" "), is(StreamingProvider.unknown));
    assertThat(StreamingProvider.from(" youtube"), is(StreamingProvider.unknown));
    assertThat(StreamingProvider.from("youtube"), is(StreamingProvider.youtube));
    assertThat(StreamingProvider.from("vImeO"), is(StreamingProvider.vimeo));
    assertThat(StreamingProvider.from("dAilyMotion"), is(StreamingProvider.dailymotion));
  }

  @Test
  void testFromUrl() {
    assertThat(StreamingProvider.fromUrl(null), is(StreamingProvider.unknown));
    assertThat(StreamingProvider.fromUrl(""), is(StreamingProvider.unknown));
    assertThat(StreamingProvider.fromUrl(" "), is(StreamingProvider.unknown));
    assertThat(StreamingProvider.fromUrl(" youtube"), is(StreamingProvider.youtube));
    assertThat(StreamingProvider.fromUrl("youtube"), is(StreamingProvider.youtube));
    assertThat(StreamingProvider.fromUrl("vImeO"), is(StreamingProvider.vimeo));
    assertThat(StreamingProvider.fromUrl("http://vImeO.be/123456789"), is(StreamingProvider.vimeo));
    assertThat(StreamingProvider.fromUrl("http://www.dailymotion.com/video/x3fd843_bever"),
        is(StreamingProvider.dailymotion));
  }

  @Test
  void testExtractStreamingId() {
    assertThat(StreamingProvider.dailymotion.extractStreamingId(
        "http://www.dailymotion.com/video/x3fd843_beverly-piegee-par-l-incroyable-strategie-de" +
            "-gilles_tv"), is("x3fd843"));
    assertThat(StreamingProvider.soundcloud.extractStreamingId(
        "https://soundcloud.com/empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien" +
            "-vermalle?in=benjamin-roux-10/sets/lazy-compagny"),
        is("empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien-vermalle?in=benjamin" +
            "-roux-10/sets/lazy-compagny"));
  }

  @Test
  void getYoutubeOembedUrl() {
    final Optional<String> oembedUrl = getOembedUrl("https://youtu.be/6xN3hSEj21Q");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://www.youtube.com/oembed?url=https://youtu.be/6xN3hSEj21Q&format=json"));
  }

  @Test
  void getVimeoOembedUrl() {
    final Optional<String> oembedUrl = getOembedUrl("http://vimeo.com/21040307");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://vimeo.com/api/oembed.json?url=http://vimeo.com/21040307"));
  }

  @Test
  void getDailymotionOembedUrl() {
    final Optional<String> oembedUrl = getOembedUrl("http://www.dailymotion.com/video/x3fgyln_jeff-bezos-fait-atterrir-en-secret-la-premiere-fusee-reutilisable_tech");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://www.dailymotion.com/services/oembed?url=http://www.dailymotion.com/video/x3fgyln"));
  }

  @Test
  void getSoundCloudOembedUrl() {
    final Optional<String> oembedUrl = getOembedUrl("https://soundcloud.com/empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien-vermalle?in=benjamin-roux-10/sets/lazy-compagny");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://soundcloud.com/oembed?url=http://soundcloud.com/empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien-vermalle?in=benjamin-roux-10/sets/lazy-compagny&format=json"));
  }
}