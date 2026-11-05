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
const css = path.join(__dirname,'../../main/webapp/discussion/jsp/css/discussion.css');
async function setup(page) {
  await page.setContent(`<main id="discussion"><button id="before">Before</button><article class="discussion-message"><div><p>Un message</p><div class="discussion-message-footer">
    <div class="discussion-actions"><details class="discussion-edit"><summary>Modifier</summary><textarea>Texte</textarea></details><button>Supprimer</button></div>
    <form class="discussion-pin-form"><button type="button">Épingler</button><span class="discussion-pin-error" hidden>Erreur</span></form>
    <button class="discussion-like-button">J’aime 2</button>
  </div></div></article><button id="after">After</button></main>`);
  await page.addStyleTag({path:css});
  await page.locator('#after').hover();
}
const opacity = (page,selector) => page.locator(selector).evaluate(el=>getComputedStyle(el).opacity);

test('desktop controls appear on hover and keyboard focus without shifting the message; an open editor stays visible',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage();await setup(page);
    assert.equal(await opacity(page,'.discussion-actions'),'0');
    assert.equal(await opacity(page,'.discussion-pin-form'),'0');
    assert.equal(await opacity(page,'.discussion-like-button'),'1');
    const before=await page.locator('.discussion-message').boundingBox();
    await page.locator('.discussion-message').hover();
    assert.equal(await opacity(page,'.discussion-actions'),'1');
    assert.equal(await opacity(page,'.discussion-pin-form'),'1');
    assert.deepEqual(await page.locator('.discussion-message').boundingBox(),before);
    await page.locator('#before').focus();await page.locator('#after').hover();
    await page.keyboard.press('Tab');
    assert.equal(await page.locator('summary').evaluate(el=>el===document.activeElement),true);
    assert.equal(await opacity(page,'.discussion-actions'),'1');
    await page.keyboard.press('Enter');
    await page.locator('#after').focus();
    assert.equal(await opacity(page,'.discussion-actions'),'1');
  } finally {await browser.close();}
});

test('touch screens always retain the edit, delete and pin actions',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true});await setup(page);
    assert.equal(await opacity(page,'.discussion-actions'),'1');
    assert.equal(await opacity(page,'.discussion-pin-form'),'1');
  } finally {await browser.close();}
});

test('like, quote, thread and icon actions share one vertical center despite native button styles',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage({viewport:{width:1100,height:850}});
    await page.setContent(`<main id="discussion"><article class="discussion-message"><div></div><div><p>Message</p><div class="discussion-message-footer">
      <form class="discussion-like-form"><button class="discussion-icon-button discussion-like-button"><svg class="discussion-icon"></svg><span>0</span></button></form>
      <button class="discussion-quote"><svg class="discussion-icon"></svg>Citer</button>
      <a class="discussion-thread-link" href="#"><svg class="discussion-icon"></svg>Créer un fil</a>
      <div class="discussion-actions"><button class="discussion-icon-button" aria-label="Proposer une idée"><svg class="discussion-icon"></svg></button></div>
      <form class="discussion-pin-form"><button class="discussion-icon-button" aria-label="Épingler"><svg class="discussion-icon"></svg></button></form>
    </div></div></article></main>`);
    await page.addStyleTag({content:'body{font:20px Arial}button{font:inherit;line-height:normal}form{margin:0}'});
    await page.addStyleTag({path:css});
    await page.locator('.discussion-message').hover();
    for(const fontSize of ['16px','20px']) {
      await page.locator('body').evaluate((n,size)=>n.style.fontSize=size,fontSize);
      const centers=await page.locator('.discussion-message-footer .discussion-icon').evaluateAll(nodes=>nodes.map(n=>{const box=n.getBoundingClientRect();return box.y+box.height/2;}));
      assert.ok(Math.max(...centers)-Math.min(...centers)<1,JSON.stringify(centers));
      const heights=await page.locator('.discussion-like-button,.discussion-quote,.discussion-thread-link').evaluateAll(nodes=>nodes.map(n=>n.getBoundingClientRect().height));
      assert.ok(Math.max(...heights)-Math.min(...heights)<1,JSON.stringify(heights));
    }
    await page.setViewportSize({width:390,height:850});
    assert.equal(await page.locator('.discussion-message-footer').evaluate(n=>n.scrollWidth<=n.clientWidth),true);
  } finally {await browser.close();}
});
