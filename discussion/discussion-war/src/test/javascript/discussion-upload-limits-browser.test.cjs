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
const root = path.join(__dirname, '../../main/webapp/discussion/jsp/javaScript');
const launch = () => chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
const file = name => ({name,mimeType:'application/octet-stream',buffer:Buffer.from('document')});
async function setup(page, {count=2, imageBytes=32, fileBytes=16}={}) {
  await page.route('https://discussion.test/**', route=>route.fulfill({contentType:'text/html; charset=utf-8',body:`
    <main id="discussion" data-files="/files" data-close-image="Fermer l’image"><form class="discussion-composer" method="post" enctype="multipart/form-data" action="/publish">
      <textarea id="discussion-body" name="body" required></textarea><div class="discussion-editor-tools"></div>
      <button type="button" class="discussion-add-file" aria-label="Joindre des fichiers">+</button>
      <input type="file" id="discussion-files" name="files" multiple data-remove="Retirer" data-error="Fichiers invalides">
      <input type="file" id="discussion-images" name="images" multiple data-error="Images invalides" data-remove="Retirer image"><div id="discussion-image-preview"></div><p id="discussion-image-error" hidden></p>
      <div id="discussion-file-preview"></div><p id="discussion-file-error" hidden></p>
      <button class="discussion-send" aria-label="Publier">Publier</button>
    </form>
    <details id="discussion-files-section"><summary>Fichiers partagés <span id="discussion-file-count">0</span></summary>
      <div id="discussion-shared-files"></div><p id="discussion-files-error" hidden></p>
    </details></main>`}));
  await page.goto('https://discussion.test/');
  await page.evaluate(()=>{
    window.handlers={};window.requests=0;window.fail=false;
    window.response='<div class="discussion-files-list" data-count="0">Aucun fichier partagé</div>';
    window.whenSilverpeasReady=fn=>fn();
    window.spServerEventSource={isEnabled:()=>true,addEventListener:(name,fn,id)=>{if(!id)throw Error('Native listener needs an ID');window.handlers[name]=fn;},removeEventListener:name=>delete window.handlers[name]};
    window.sp={ajaxRequest:()=>({send:async()=>{window.requests++;if(window.fail)throw Error('offline');return {responseText:window.response};}})};
  });
  await page.evaluate(({count,imageBytes,fileBytes})=>{
    const images=document.getElementById('discussion-images');images.dataset.maxCount=count;images.dataset.maxBytes=imageBytes;
    document.getElementById('discussion-files').dataset.maxBytes=fileBytes;
  },{count,imageBytes,fileBytes});
  for(const script of ['discussion-images.js','discussion-composer.js','discussion-files.js','discussion-file-picker.js'])await page.addScriptTag({path:path.join(root,script)});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  await page.waitForFunction(()=>document.querySelector('.discussion-files-list'));
}


test('instance image count and byte limits apply to both selection and clipboard without discarding previous images',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    const image=(name,size)=>({name,mimeType:'image/png',buffer:Buffer.alloc(size,1)});
    await page.locator('#discussion-images').setInputFiles(image('one.png',32));
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),1);
    await page.locator('#discussion-images').setInputFiles(image('too-big.png',33));
    assert.equal(await page.locator('#discussion-image-error').isVisible(),true);
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files[0].name),'one.png');
    await page.evaluate(()=>{
      const clipboard=new DataTransfer();clipboard.items.add(new File([new Uint8Array(20)],'two.png',{type:'image/png'}));
      document.getElementById('discussion-body').dispatchEvent(new ClipboardEvent('paste',{clipboardData:clipboard,bubbles:true,cancelable:true}));
    });
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),2);
    await page.locator('#discussion-images').setInputFiles(image('three.png',1));
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),2);
    assert.equal(await page.locator('#discussion-image-error').isVisible(),true);
    await page.locator('#discussion-body').fill('Brouillon conservé');
    assert.equal(await page.locator('#discussion-body').inputValue(),'Brouillon conservé');
  } finally {await browser.close();}
});

test('larger instance image counts are accepted and file byte limits replace the default',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page,{count:6,imageBytes:50,fileBytes:16});
    await page.locator('#discussion-images').setInputFiles(Array.from({length:6},(_,i)=>({name:i+'.png',mimeType:'image/png',buffer:Buffer.alloc(50,1)})));
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),6);
    assert.equal(await page.locator('#discussion-image-error').isVisible(),false);
    await page.locator('#discussion-files').setInputFiles({name:'valid.pdf',mimeType:'application/pdf',buffer:Buffer.alloc(16)});
    assert.equal(await page.locator('#discussion-files').evaluate(n=>n.files.length),1);
    await page.locator('#discussion-files').setInputFiles({name:'large.pdf',mimeType:'application/pdf',buffer:Buffer.alloc(17)});
    assert.equal(await page.locator('#discussion-file-error').isVisible(),true);
    assert.equal(await page.locator('#discussion-files').evaluate(n=>n.files[0].name),'valid.pdf');
  } finally {await browser.close();}
});
