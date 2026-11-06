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
const path = require('node:path');
const {chromium} = require('playwright');
const sharedEditor = require('./shared-editor-assets.cjs');
const scripts = path.join(__dirname, '../../main/webapp/discussion/jsp/javaScript');

test('presence refresh uses the configured interval instead of starting a fixed polling timer', async () => {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page = await browser.newPage();
    await page.setContent('<main id="discussion" data-presence-refresh-ms="45000"><section id="discussion-participants"></section><p id="discussion-presence-error" hidden></p><section id="discussion-history"><div class="discussion-page" data-more="false"></div></section><div id="discussion-scroll"></div><button id="discussion-older"></button><p id="discussion-error" hidden></p></main>');
    await page.evaluate(() => {
      window.timers = [];
      window.setInterval = (callback, delay) => {timers.push(delay); return 1;};
      window.requestAnimationFrame = () => 1;
    });
    await page.addScriptTag({path:path.join(scripts, 'discussion.js')});
    await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
    assert.deepEqual(await page.evaluate(() => timers), [45000]);
  } finally {await browser.close();}
});

test('pinned message previews use the configured Unicode-safe excerpt length', async () => {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page = await browser.newPage();
    await page.setContent('<main id="discussion" data-pin-excerpt-length="8"><span class="discussion-pin-excerpt" data-pin-text="' + '😀'.repeat(25) + '"></span></main>');
    await sharedEditor.load(page);
    await page.addScriptTag({path:path.join(scripts, 'discussion-editor.js')});
    await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
    assert.equal(await page.locator('.discussion-pin-excerpt').textContent(), '😀'.repeat(8) + '…');
  } finally {await browser.close();}
});
