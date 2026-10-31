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
async function setup(page) {
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
  for(const script of ['discussion-images.js','discussion-composer.js','discussion-files.js','discussion-file-picker.js'])await page.addScriptTag({path:path.join(root,script)});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  await page.waitForFunction(()=>document.querySelector('.discussion-files-list'));
}

test('the paperclip supports file-only multipart messages and removing files restores empty-message validation',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),false);
    const chooser=page.waitForEvent('filechooser');
    await page.getByRole('button',{name:'Joindre des fichiers',exact:true}).click();
    await (await chooser).setFiles([file('budget.pdf'),file('notes.txt')]);
    assert.equal(await page.locator('#discussion-body').evaluate(el=>el.required),false);
    assert.equal(await page.locator('.discussion-editor-tools .discussion-add-file').count(),1);
    await page.getByRole('button',{name:'Retirer : notes.txt',exact:true}).click();
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),true);
    const request=page.waitForRequest(r=>r.method()==='POST');
    await page.getByRole('button',{name:'Publier',exact:true}).click();
    const posted=await request;
    assert.match(posted.headers()['content-type'],/^multipart\/form-data; boundary=/);
    assert.match(posted.postDataBuffer().toString('latin1'),/filename="budget.pdf"/);
    assert.doesNotMatch(posted.postDataBuffer().toString('latin1'),/filename="notes.txt"/);
  } finally {await browser.close();}
});

test('file selection limits preserve previous files and the last removal disables an empty send',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.locator('#discussion-files').setInputFiles([file('1.pdf'),file('2.pdf'),file('3.pdf'),file('4.pdf')]);
    await page.locator('#discussion-files').setInputFiles(file('5.pdf'));
    assert.equal(await page.locator('#discussion-file-error').isVisible(),true);
    assert.equal(await page.locator('#discussion-files').evaluate(el=>el.files.length),4);
    for(let i=1;i<=4;i++)await page.getByRole('button',{name:'Retirer : '+i+'.pdf',exact:true}).click();
    assert.equal(await page.locator('#discussion-body').evaluate(el=>el.required),true);
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),false);
  } finally {await browser.close();}
});

test('the shared files accordion refreshes on remote changes and keeps a link to the original message',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.locator('#discussion-files-section summary').click();
    await page.evaluate(()=>{
      window.response='<div class="discussion-files-list" data-count="1"><a class="discussion-file-download" href="/silverpeas/attached_file/file" download="budget.pdf">budget.pdf</a><a class="discussion-file-origin" href="/Rdiscussion/discussion1/messages/old-message">Voir le message</a></div>';
      document.dispatchEvent(new Event('discussion:changed'));
    });
    await page.waitForFunction(()=>document.getElementById('discussion-file-count').textContent==='1');
    assert.equal(await page.locator('.discussion-file-download').getAttribute('download'),'budget.pdf');
    assert.equal(await page.locator('.discussion-file-origin').getAttribute('href'),'/Rdiscussion/discussion1/messages/old-message');
    assert.equal(await page.locator('#discussion-files-section').getAttribute('open'),'');
    await page.evaluate(()=>{window.response='<div class="discussion-files-list" data-count="0">Aucun fichier partagé</div>';window.handlers.OPEN_EVENT_SOURCE();});
    await page.waitForFunction(()=>document.getElementById('discussion-file-count').textContent==='0');
  } finally {await browser.close();}
});


test('mixed photos and files keep image-only or file-only sends valid until the final attachment is removed',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.locator('#discussion-files').setInputFiles(file('budget.pdf'));
    await page.locator('#discussion-images').setInputFiles({name:'photo.png',mimeType:'image/png',buffer:Buffer.from('photo fixture')});
    await page.getByRole('button',{name:'Retirer : budget.pdf',exact:true}).click();
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),true);
    assert.equal(await page.locator('#discussion-body').evaluate(el=>el.required),false);
    await page.locator('#discussion-files').setInputFiles(file('budget.pdf'));
    await page.getByRole('button',{name:'Retirer image : photo.png',exact:true}).click();
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),true);
    assert.equal(await page.locator('#discussion-body').evaluate(el=>el.required),false);
    await page.getByRole('button',{name:'Retirer : budget.pdf',exact:true}).click();
    assert.equal(await page.getByRole('button',{name:'Publier',exact:true}).isEnabled(),false);
    assert.equal(await page.locator('#discussion-body').evaluate(el=>el.required),true);
  } finally {await browser.close();}
});
