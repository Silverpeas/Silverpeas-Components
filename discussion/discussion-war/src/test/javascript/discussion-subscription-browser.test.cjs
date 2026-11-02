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
const fs = require('node:fs');
const {chromium} = require('playwright');

test('QVF uses the native manager and blocks repeated requests until completion', async () => {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page = await browser.newPage();
    const jsp = fs.readFileSync(path.join(__dirname,'../../main/webapp/discussion/jsp/discussion.jsp'),'utf8');
    const operation = jsp.match(/<view:operation altText="([^"]*)"[^>]*action="#discussion-subscription"\/>/);
    assert.ok(operation, 'QVF subscription operation exists');
    await page.setContent(`<main id="discussion" data-instance-id="discussion42"></main><a href="#discussion-subscription">${operation[1]}</a><p id="discussion-error" hidden>Erreur</p>`);
    assert.equal(await page.locator('#subscriptionMenuLabel').count(),1,
      'The JSP operation must render an HTML label, rather than escaped markup');
    await page.evaluate(() => {
      window.SUBSCRIPTION_PROMISE = Promise.resolve();
      window.calls = [];
      window.SilverpeasSubscriptionManager = function(options) {
        window.options = options;
        document.getElementById('subscriptionMenuLabel').textContent = 'S’abonner';
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
