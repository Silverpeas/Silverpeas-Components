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
const script = path.join(__dirname, '../../main/webapp/discussion/jsp/javaScript/discussion-images.js');
const png = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=', 'base64');
const photo = name => ({name,mimeType:'image/png',buffer:png});
async function setup(page) {
  await page.route('https://discussion.test/**', route => {
    if(route.request().url().endsWith('/image.png'))return route.fulfill({contentType:'image/png',body:png});
    return route.fulfill({contentType:'text/html; charset=utf-8',body:`
      <main id="discussion" data-close-image="Fermer l’image">
        <div class="discussion-message-images" style="min-height:80px"><a href="/image.png" aria-label="Ouvrir l’image en grand"><img alt="Image jointe" src="/image.png" width="80" height="60"></a></div>
        <form class="discussion-composer" action="/publish" method="post" enctype="multipart/form-data">
          <textarea name="body" id="discussion-body" required></textarea>
          <div class="discussion-editor-tools"></div><button type="button" class="discussion-add-image" aria-label="Ajouter des photos" title="4 images maximum · 5 Mo chacune"><svg></svg></button>
          <input type="file" name="images" id="discussion-images" multiple data-remove="Retirer" data-error="Images invalides">
          <div id="discussion-image-preview"></div><p id="discussion-image-error" hidden></p><button>Publier</button>
        </form>
      </main>`});
  });
  await page.goto('https://discussion.test/');
  await page.evaluate(()=>{window.revoked=[];const revoke=URL.revokeObjectURL.bind(URL);URL.revokeObjectURL=url=>{window.revoked.push(url);revoke(url);};});
  await page.addScriptTag({path:script});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
}

test('photos are previewed, accumulated and removable; image-only messages use multipart upload',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage();await setup(page);
    const chooser = page.waitForEvent('filechooser');
    await page.getByRole('button',{name:'Ajouter des photos',exact:true}).click();
    await (await chooser).setFiles(photo('first.png'));
    assert.equal(await page.locator('.discussion-editor-tools .discussion-add-image').count(),1);
    assert.equal(await page.locator('.discussion-add-image').textContent(),'');
    assert.equal(await page.locator('#discussion-body').evaluate(el=>el.required),false);
    await page.locator('#discussion-images').setInputFiles(photo('second.png'));
    assert.equal(await page.locator('#discussion-image-preview img').count(),2);
    await page.getByRole('button',{name:'Retirer : first.png',exact:true}).click();
    assert.equal(await page.locator('#discussion-images').evaluate(el=>Array.from(el.files).map(f=>f.name).join(',')),'second.png');
    assert.ok(await page.evaluate(()=>window.revoked.length>=2));
    const request=page.waitForRequest(r=>r.method()==='POST'&&r.url().endsWith('/publish'));
    await page.getByRole('button',{name:'Publier',exact:true}).click();
    const posted=await request;
    assert.match(posted.headers()['content-type'],/^multipart\/form-data; boundary=/);
    assert.match(posted.postDataBuffer().toString('latin1'),/filename="second.png"/);
    assert.doesNotMatch(posted.postDataBuffer().toString('latin1'),/filename="first.png"/);
  } finally {await browser.close();}
});

test('invalid files and excess photos retain the previous selection and restore text validation after removal',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage();await setup(page);
    await page.locator('#discussion-images').setInputFiles([photo('1.png'),photo('2.png'),photo('3.png'),photo('4.png')]);
    await page.locator('#discussion-images').setInputFiles(photo('5.png'));
    assert.equal(await page.locator('#discussion-image-error').isVisible(),true);
    assert.equal(await page.locator('#discussion-images').evaluate(el=>el.files.length),4);
    for(let i=1;i<=4;i++)await page.getByRole('button',{name:'Retirer : '+i+'.png',exact:true}).click();
    assert.equal(await page.locator('#discussion-body').evaluate(el=>el.required),true);
    await page.locator('#discussion-images').setInputFiles({name:'active.svg',mimeType:'image/svg+xml',buffer:Buffer.from('<svg/>')});
    assert.equal(await page.locator('#discussion-images').evaluate(el=>el.files.length),0);
    await page.locator('#discussion-images').setInputFiles({name:'large.png',mimeType:'image/png',buffer:Buffer.alloc(5*1024*1024+1)});
    assert.equal(await page.locator('#discussion-images').evaluate(el=>el.files.length),0);
  } finally {await browser.close();}
});

test('message images open in an accessible modal and close with Escape',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage();await setup(page);
    await page.getByRole('link',{name:'Ouvrir l’image en grand',exact:true}).click();
    assert.equal(await page.getByRole('dialog',{name:'Ouvrir l’image en grand'}).isVisible(),true);
    assert.equal(await page.locator('dialog img').getAttribute('src'),'https://discussion.test/image.png');
    await page.keyboard.press('Escape');
    assert.equal(await page.locator('dialog').isVisible(),false);
    await page.waitForFunction(()=>!document.querySelector('dialog img').hasAttribute('src'));
    assert.equal(await page.locator('dialog img').getAttribute('src'),null);
  } finally {await browser.close();}
});
