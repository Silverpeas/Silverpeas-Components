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
const fs = require('node:fs');
const path = require('node:path');
const root = path.join(__dirname, '../../../..');
const bundles = path.join(root, 'discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/multilang');
function files(directory) {
  return fs.readdirSync(directory, {withFileTypes:true}).flatMap(entry => {
    const file = path.join(directory, entry.name);
    return entry.isDirectory() ? files(file) : [file];
  });
}
test('all Discussion translation keys used by production sources exist in every supported language', () => {
  const sources = ['discussion-war', 'discussion-library'].flatMap(module => files(path.join(root, module, 'src/main')))
    .filter(file => /\.(jsp|java|js)$/.test(file));
  const keys = new Set(sources.flatMap(file => [...fs.readFileSync(file, 'utf8').matchAll(/["'](discussion\.[A-Za-z0-9_.]+)["']/g)]
    .map(match => match[1])).filter(key => key !== 'discussion.jsp'));
  assert.ok(keys.size > 150);
  for (const locale of ['', '_fr', '_en', '_de']) {
    const properties = fs.readFileSync(path.join(bundles, 'DiscussionBundle' + locale + '.properties'), 'utf8');
    const values = new Map([...properties.matchAll(/^([^#!\s][^=\s]*)\s*=([^\n]*)/gm)].map(match => [match[1], match[2].trim()]));
    for (const key of keys) { assert.ok(values.get(key), `${locale || 'default'}: ${key}`); }
  }
});
