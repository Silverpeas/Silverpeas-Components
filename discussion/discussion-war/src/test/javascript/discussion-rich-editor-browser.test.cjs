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
const test=require('node:test');
const assert=require('node:assert/strict');
const path=require('node:path');
const fs=require('node:fs');
const {chromium}=require('playwright');
const assets=path.join(__dirname,'../../main/webapp/discussion/jsp');
const props=fs.readFileSync(path.join(__dirname,'../../../../discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/multilang/DiscussionBundle_fr.properties'),'utf8');
const labels=Object.fromEntries(props.split('\n').filter(s=>s.startsWith('discussion.format')).map(s=>{const i=s.indexOf('=');return [s.slice(11,i).trim(),s.slice(i+1).trim()];}));
async function setup({body='',draft=''}={}) {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width:1100,height:850}});
  await page.setContent(`<base href="https://discussion.test/"><main id="discussion" data-profile-base="/Rprofil/jsp/Main" data-contribution-base="/silverpeas/Contribution/" data-view-idea-label="Voir l’idée" data-view-event-label="Voir l’événement" data-view-vote-label="Participer au vote" data-emoji-label="Emoji" data-mention-label="Mention" data-icons="/icons.svg" data-close-image="Fermer" data-length-error="Trop long" data-message-required="Message requis" data-link-text="Texte" data-link-url="Adresse" data-link-apply="Appliquer" data-link-invalid="Adresse invalide"><template id="discussion-mention-directory"><span data-user-id="2" data-name="Alice Martin"></span><span data-user-id="3" data-name="Bernard"></span></template><div class="discussion-body"></div><section id="later"></section><form class="discussion-composer" method="post" enctype="multipart/form-data"><div class="discussion-compose-box"><textarea id="discussion-body" name="body" maxlength="2000" required></textarea><input hidden type="file" name="images" id="discussion-images" multiple data-remove="Retirer" data-error="Images invalides"><input hidden type="file" name="files" id="discussion-files" multiple><div id="discussion-image-preview"></div><p id="discussion-image-error" hidden></p><div class="discussion-composer-footer"><button class="discussion-send" type="submit">Publier</button></div></div></form></main>`);
  await page.evaluate(({body,draft,labels})=>{
    const main=document.getElementById('discussion');
    Object.entries(labels).forEach(([key,value])=>{main.dataset[key]=value;});
    document.querySelector('.discussion-body').textContent=body;
    document.getElementById('discussion-body').value=draft;
    window.posts=[];document.addEventListener('submit',event=>{event.preventDefault();posts.push(new FormData(event.target));});
  },{body,draft,labels});
  await page.addStyleTag({path:path.join(assets,'css/discussion.css')});
  for(const file of ['vendor/markdown-it-15.0.3.min.js','discussion-markdown.js','vendor/tiptap-3.32.2.min.js','discussion-rich-editor.js','discussion-editor.js','discussion-images.js','discussion-composer.js']) {
    await page.addScriptTag({path:path.join(assets,'javaScript',file)});
  }
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}

const visual = page => page.locator('.discussion-composer .tiptap');
async function selectAll(page) { await visual(page).click(); await page.keyboard.press('Control+a'); }
async function source(page) { return page.locator('input[name=body]').inputValue(); }

test('visual editing loads existing Markdown and mentions without exposing syntax, and posts Markdown', async () => {
  const {browser,page} = await setup({draft:'**Décision** et @[Alice Martin](2)\n\n- Qwen\n- Mistral'});
  try {
    assert.equal(await page.locator('textarea').isVisible(),false);
    assert.equal(await visual(page).locator('strong').textContent(),'Décision');
    assert.equal(await visual(page).locator('.discussion-mention').textContent(),'@Alice Martin');
    assert.equal(await visual(page).locator('li').count(),2);
    assert.ok(!(await visual(page).innerText()).includes('**'));
    assert.ok(!(await visual(page).innerText()).includes('(2)'));
    await selectAll(page);
    await page.keyboard.press('Control+b');
    assert.ok((await source(page)).includes('@[Alice Martin](2)'));
    await page.locator('.discussion-send').click();
    const posted = await page.evaluate(()=>posts[0].get('body'));
    assert.ok(posted.includes('@[Alice Martin](2)'));
    assert.ok(!posted.includes('<strong>'));
    assert.equal(await page.evaluate(()=>posts[0].has('bodyDisplay')),false);
  } finally { await browser.close(); }
});

test('format controls keep selection and show active state; undo and redo preserve content', async () => {
  const {browser,page} = await setup({draft:'Bonjour'});
  try {
    await page.locator('.discussion-format-toggle').click();
    await selectAll(page);
    await page.locator('.discussion-format-bold').click();
    assert.equal(await visual(page).locator('strong').textContent(),'Bonjour');
    assert.equal(await page.locator('.discussion-format-bold').getAttribute('aria-pressed'),'true');
    assert.equal(await source(page),'**Bonjour**');
    await page.keyboard.press('Control+z');
    assert.equal(await source(page),'Bonjour');
    await page.keyboard.press('Control+Shift+z');
    assert.equal(await source(page),'**Bonjour**');
    await page.locator('.discussion-format-bullets').click();
    assert.equal(await visual(page).locator('ul li').count(),1);
    for (const width of [1100,390,320]) {
      await page.setViewportSize({width,height:850});
      const box = await page.locator('.discussion-compose-box').boundingBox();
      const text = await visual(page).boundingBox();
      assert.ok(text.width > box.width * .85);
      const tools = await page.locator('.discussion-editor-tools').boundingBox();
      assert.ok(tools.y >= text.y + text.height);
      assert.equal(await page.locator('.discussion-editor-tools').evaluate(n=>n.scrollWidth<=n.clientWidth),true);
      assert.equal(await page.locator('.discussion-format-tools').evaluate(n=>n.scrollWidth<=n.clientWidth),true);
    }
  } finally { await browser.close(); }
});

test('mention completion is keyboard accessible and retains the recipient through formatting and deletion', async () => {
  const {browser,page} = await setup();
  try {
    await visual(page).click();
    await page.keyboard.type('Salut @Ali');
    assert.equal(await page.locator('.discussion-mention-option').textContent(),'@Alice Martin');
    await page.keyboard.press('Enter');
    assert.ok((await source(page)).includes('@[Alice Martin](2)'));
    await selectAll(page);
    await page.keyboard.press('Control+i');
    assert.ok((await source(page)).includes('@[Alice Martin](2)'));
    await page.keyboard.press('Backspace');
    assert.equal(await source(page),'');
    assert.equal(await visual(page).locator('.discussion-mention').count(),0);
    await page.locator('.discussion-editor-tools > button').nth(1).click();
    await page.keyboard.type('Ber');
    await page.keyboard.press('Enter');
    assert.ok((await source(page)).includes('@[Bernard](3)'));
  } finally { await browser.close(); }
});

test('named link popover validates URLs and stores a named Markdown link', async () => {
  const {browser,page} = await setup({draft:'Documentation'});
  try {
    await selectAll(page);
    await page.locator('.discussion-format-toggle').click();
    await page.locator('.discussion-format-link').click();
    await page.locator('.discussion-link-editor input[type=url]').fill('javascript:alert(1)');
    await page.locator('.discussion-link-apply').click();
    assert.equal(await page.locator('.discussion-link-editor [role=status]').textContent(),'Adresse invalide');
    await page.locator('.discussion-link-editor input[type=url]').fill('https://example.org/guide');
    await page.locator('.discussion-link-apply').click();
    assert.equal(await visual(page).locator('a').textContent(),'Documentation');
    assert.equal(await source(page),'[Documentation](https://example.org/guide)');
    await page.keyboard.type(' suite');
    assert.ok((await source(page)).includes('suite'));
  } finally { await browser.close(); }
});

test('clipboard images use the native attachment pipeline from the visual editor', async () => {
  const {browser,page} = await setup();
  try {
    const prevented = await visual(page).evaluate(n=>{
      const data=new DataTransfer();data.items.add(new File([new Uint8Array(20)],'capture.png',{type:'image/png'}));
      const event=new ClipboardEvent('paste',{clipboardData:data,bubbles:true,cancelable:true});n.dispatchEvent(event);return event.defaultPrevented;
    });
    assert.equal(prevented,true);
    assert.equal(await page.locator('#discussion-image-preview img').count(),1);
    assert.equal(await visual(page).locator('img').count(),0);
    await page.locator('.discussion-send').click();
    assert.equal(await page.evaluate(()=>posts[0].get('images').name),'capture.png');
    assert.equal(await page.evaluate(()=>posts[0].get('body')),'');
  } finally { await browser.close(); }
});

test('draft restoration, cancellation reset, canonical length limit and empty submission remain safe', async () => {
  const {browser,page} = await setup();
  try {
    await page.evaluate(()=>{
      const textarea=document.querySelector('textarea');textarea.value='**Brouillon** @[Alice Martin](2)';textarea.dispatchEvent(new Event('input',{bubbles:true}));
    });
    assert.equal(await visual(page).locator('strong').textContent(),'Brouillon');
    await page.evaluate(()=>DiscussionEditor.reset(document.querySelector('textarea'),'*Rétabli* @[Alice Martin](2)'));
    assert.equal(await visual(page).locator('em').textContent(),'Rétabli');
    assert.ok((await source(page)).includes('@[Alice Martin](2)'));
    await page.evaluate(()=>DiscussionEditor.reset(document.querySelector('textarea'),'a'.repeat(2000)));
    await visual(page).click();await page.keyboard.press('Control+End');await page.keyboard.type('b');
    assert.equal((await source(page)).length,2000);
    assert.equal(await page.locator('.discussion-rich-error').isVisible(),true);
    await page.keyboard.press('Backspace');
    assert.equal((await source(page)).length,1999);
    await page.evaluate(()=>DiscussionEditor.reset(document.querySelector('textarea'),''));
    await page.evaluate(()=>document.querySelector('form').requestSubmit());
    assert.equal(await page.evaluate(()=>posts.length),0);
    assert.equal(await page.locator('.discussion-rich-error').textContent(),'Message requis');
    assert.equal(await visual(page).evaluate(n=>n===document.activeElement),true);
  } finally { await browser.close(); }
});

test('new history editors use the same visual editing integration once and destroy cleanly on removal', async () => {
  const {browser,page} = await setup();
  try {
    await page.evaluate(()=>{
      const section=document.getElementById('later');section.innerHTML='<form><textarea name="body" maxlength="2000">**Historique**</textarea></form>';
      DiscussionEditor.enhance(section);DiscussionEditor.enhance(section);
    });
    assert.equal(await page.locator('#later .tiptap').count(),1);
    assert.equal(await page.locator('#later strong').textContent(),'Historique');
    await page.evaluate(()=>document.getElementById('later').replaceChildren());
    assert.equal(await page.locator('#later .tiptap').count(),0);
  } finally { await browser.close(); }
});


test('pasted HTML stays within the supported schema, and image captions are retained', async () => {
  const {browser,page} = await setup();
  try {
    await visual(page).click();
    await visual(page).evaluate(n=>{
      const data=new DataTransfer();
      data.setData('text/html','<p><strong>Collé</strong> <a href="javascript:alert(1)">piège</a><img src="https://example.org/tracker.png"><script>window.attacked=true</script></p>');
      data.setData('text/plain','Collé piège');
      n.dispatchEvent(new ClipboardEvent('paste',{clipboardData:data,bubbles:true,cancelable:true}));
    });
    assert.equal(await visual(page).locator('strong').textContent(),'Collé');
    assert.equal(await visual(page).locator('img, script, a[href^="javascript:"]').count(),0);
    assert.equal(await page.evaluate(()=>window.attacked),undefined);
    await page.evaluate(()=>DiscussionEditor.reset(document.querySelector('textarea'),''));
    await visual(page).evaluate(n=>{
      const data=new DataTransfer();data.items.add(new File([new Uint8Array(20)],'caption.png',{type:'image/png'}));
      data.setData('text/plain','Une légende');
      n.dispatchEvent(new ClipboardEvent('paste',{clipboardData:data,bubbles:true,cancelable:true}));
    });
    assert.equal(await source(page),'Une légende');
    assert.equal(await page.locator('#discussion-image-preview img').count(),1);
  } finally { await browser.close(); }
});
