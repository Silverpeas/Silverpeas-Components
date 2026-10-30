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
const { tokenize, mentionQuery, filterPeople } = require('../../main/webapp/discussion/jsp/javaScript/discussion-editor.js');

test('links and canonical mentions are identified without interpreting HTML or losing emoji', () => {
  const tokens = tokenize('Bonjour @[Alice](2) 😀 https://example.org/a?b=1&c=2. <img src=x onerror=alert(1)>');
  assert.equal(tokens.find(t => t.type === 'mention').id, '2');
  assert.equal(tokens.find(t => t.type === 'link').href, 'https://example.org/a?b=1&c=2');
  assert.ok(tokens.filter(t => t.type === 'text').map(t => t.text).join('').includes('<img src=x onerror=alert(1)>'));
  assert.ok(tokens.some(t => t.text.includes('😀')));
});

test('executable and non-web protocols remain plain text', () => {
  assert.equal(tokenize('javascript:alert(1) data:text/html,test file:///etc/passwd').filter(t => t.type === 'link').length, 0);
  assert.equal(tokenize('https://').filter(t => t.type === 'link').length, 0);
});

test('mention completion respects the caret and matches accents', () => {
  assert.deepEqual(mentionQuery('Bonjour @Él', 11), {query: 'Él', start: 8, end: 11});
  assert.equal(mentionQuery('mail@example.org', 16), null);
  assert.equal(mentionQuery('@[Alice](2)', 11), null);
  assert.deepEqual(filterPeople([{id:'2',name:'Élodie Martin'},{id:'3',name:'Bob'}], 'elodie').map(p => p.id), ['2']);
});


test('balanced parentheses in URLs are preserved while surrounding punctuation is excluded', () => {
  const tokens = tokenize('(https://example.org/wiki/A_(B)).');
  assert.equal(tokens.find(t => t.type === 'link').href, 'https://example.org/wiki/A_(B)');
});
