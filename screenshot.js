const { chromium } = require('playwright');
const path = require('path');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();

  const files = [
    { html: 'architecture.html', png: 'architecture.png', width: 960 },
    { html: 'flowchart.html',    png: 'flowchart.png',    width: 760 },
    { html: 'er-diagram.html',   png: 'er-diagram.png',   width: 1160 },
  ];

  const base = 'D:/myproject/wfxyend/a-a-a';

  for (const f of files) {
    await page.setViewportSize({ width: f.width, height: 900 });
    await page.goto('file:///' + base + '/' + f.html, { waitUntil: 'networkidle' });
    const bodyH = await page.evaluate(() => document.body.scrollHeight);
    await page.setViewportSize({ width: f.width, height: bodyH + 20 });
    await page.screenshot({ path: base + '/' + f.png, fullPage: true });
    console.log('Saved: ' + f.png);
  }

  await browser.close();
})();
