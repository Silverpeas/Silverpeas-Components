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
const root = path.join(__dirname, '../../main/webapp/discussion/jsp');
const launch = () => chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
async function setup(page) {
  await page.setContent(`<main id="discussion"><form class="discussion-composer"><label for="discussion-body" class="discussion-compose-label">Écrire un message</label><div class="discussion-compose-box">
    <textarea id="discussion-body" rows="1" required></textarea><div class="discussion-editor-tools"><button type="button">Emoji</button></div>
    <div class="discussion-image-picker"><input id="discussion-images" type="file" multiple><div id="discussion-image-preview"></div></div>
    <div class="discussion-composer-footer"><span class="discussion-compose-label">2 000 caractères maximum</span><button type="submit" class="discussion-send" aria-label="Publier"><svg></svg></button></div>
  </div></form></main>`);
  await page.addStyleTag({path:path.join(root,'css/discussion.css')});
  await page.addStyleTag({content:'.discussion-composer{position:static!important;width:auto!important}body{margin:0}'});
  await page.addScriptTag({path:path.join(root,'javaScript/discussion-composer.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
}

test('the compact composer grows with the draft, disables empty sends and preserves mobile alignment',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage({viewport:{width:1100,height:900}});await setup(page);
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),false);
    const initial=await page.locator('#discussion-body').boundingBox();
    await page.locator('#discussion-body').fill('Une idée\nUne précision\nUne autre ligne\nEncore une ligne');
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),true);
    const expanded=await page.locator('#discussion-body').boundingBox();
    assert.ok(expanded.height>initial.height);
    await page.locator('#discussion-body').fill('  ');
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),false);
    for(const width of [390,320]) {
      await page.setViewportSize({width,height:900});
      assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
      const tools=await page.locator('.discussion-editor-tools').boundingBox();
      const send=await page.locator('.discussion-send').boundingBox();
      assert.ok(Math.abs(tools.y+tools.height-send.y-send.height)<2);
    }
  } finally {await browser.close();}
});

test('an image enables an image-only send and removing its preview disables the empty composer',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.locator('#discussion-images').setInputFiles({name:'photo.png',mimeType:'image/png',buffer:Buffer.from('fixture')});
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),true);
    await page.evaluate(()=>{
      const preview=document.getElementById('discussion-image-preview');
      preview.append(document.createElement('img'));
      document.getElementById('discussion-images').value='';
      preview.replaceChildren();
    });
    await page.waitForFunction(()=>document.querySelector('.discussion-send').disabled);
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),false);
  } finally {await browser.close();}
});
