/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
const test = require('node:test');
const assert = require('node:assert/strict');
const {renderHtml} = require('../../main/webapp/discussion/jsp/javaScript/discussion-markdown.js');

test('common standalone smileys become emojis, also next to punctuation and within Markdown formatting', () => {
  const html=renderHtml("Une idée :) ;) :D :-( :P :o :/ :'( <3\n**:-)** et (;-)) :D!");
  for(const emoji of ['🙂','😉','😄','🙁','😛','😮','😕','😢','❤️']) { assert.ok(html.includes(emoji),emoji); }
  assert.ok(html.includes('<strong>🙂</strong>'));
  assert.ok(html.includes('(😉)'));
  assert.ok(html.includes('😄!'));
});

test('URLs, explicit links, inline code, fenced code and canonical mentions retain their original text', () => {
  const html=renderHtml('https://example.org/:) [Lien :)](https://example.org/:D) `:)`\n\n```\n;) :D\n```\n\n@[Alice :D](2)');
  assert.ok(html.includes('https://example.org/:)'));
  assert.ok(html.includes('href="https://example.org/:D"'));
  assert.ok(html.includes('Lien :)</a>'));
  assert.ok(html.includes('<code>:)</code>'));
  assert.ok(html.includes(';) :D\n</code>'));
  assert.ok(html.includes('@Alice :D</span>'));
  assert.ok(!html.includes('🙂'));
});

test('embedded punctuation is not mistaken for a smiley and rendering does not mutate the stored source', () => {
  const source='texte:) identifiant:D :Dashboard C:/temp https://host/path:/ :)';
  const html=renderHtml(source);
  assert.ok(html.includes('texte:) identifiant:D :Dashboard C:/temp https://host/path:/ 🙂'));
  assert.equal(source,'texte:) identifiant:D :Dashboard C:/temp https://host/path:/ :)');
});
