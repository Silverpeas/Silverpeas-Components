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

test('QVF uses the native manager and blocks repeated requests until completion', async () => {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page = await browser.newPage();
    await page.setContent(`<main id="discussion" data-instance-id="discussion42"></main><a href="#discussion-subscription"><span id="subscriptionMenuLabel">S’abonner</span></a><p id="discussion-error" hidden>Erreur</p>`);
    await page.evaluate(() => {
      window.SUBSCRIPTION_PROMISE = Promise.resolve();
      window.calls = [];
      window.SilverpeasSubscriptionManager = function(options) {
        window.options = options;
        this.switchUserSubscription = () => {
          window.calls.push('switch');
          return new Promise((resolve,reject) => {window.complete=resolve; window.fail=reject;});
        };
      };
    });
    await page.addScriptTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-subscription.js')});
    await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
    await page.locator('#subscriptionMenuLabel').click();
    await page.locator('#subscriptionMenuLabel').click();
    assert.equal(await page.evaluate(() => window.calls.length),1);
    assert.equal(await page.evaluate(() => window.options.componentInstanceId),'discussion42');
    assert.equal(await page.evaluate(() => location.hash),'');
    await page.evaluate(() => window.complete());
    await page.locator('#subscriptionMenuLabel').click();
    await page.evaluate(() => window.fail(new Error('offline')));
    assert.equal(await page.locator('#discussion-error').isVisible(),true);
    await page.locator('#subscriptionMenuLabel').click();
    assert.equal(await page.evaluate(() => window.calls.length),3);
  } finally {await browser.close();}
});
