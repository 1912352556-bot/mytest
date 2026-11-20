const { chromium, devices } = require('playwright');
const fs = require('fs');

const BASE = 'http://localhost:8088';
const OUT = 'D:/myproject/wfxyend/a-a-a/docs/mobile-preview';
fs.mkdirSync(OUT, { recursive: true });

(async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext({ ...devices['iPhone 13'], locale: 'zh-CN' });
  const page = await context.newPage();

  const errors = [];
  page.on('console', (m) => {
    if (m.type() === 'error') errors.push(m.text());
  });
  page.on('pageerror', (e) => errors.push('PAGEERROR ' + e.message));

  const shot = async (name, fullPage = false) => {
    await page.screenshot({ path: `${OUT}/${name}`, fullPage });
    console.log('saved ' + name);
  };

  const login = async (username) => {
    await page.goto(`${BASE}/m/login`, { waitUntil: 'networkidle' });
    const inputs = page.locator('input');
    await inputs.nth(0).fill(username);
    await inputs.nth(1).fill('123456');
    await page.getByRole('button', { name: /登/ }).click();
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1200);
  };

  // 1. 登录页
  await page.goto(`${BASE}/m/login`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(600);
  await shot('mobile-1-login.png');

  // 2. 客户首页
  await login('customer01');
  console.log('after customer login url=' + page.url());
  await shot('mobile-2-customer-home.png');

  // 3. 客户订单列表
  await page.goto(`${BASE}/m/customer/orders`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(900);
  await shot('mobile-3-customer-orders.png');

  // 4. 客户订单详情（点第一条）
  const firstOrder = page.locator('.m-order').first();
  if (await firstOrder.count()) {
    await firstOrder.click();
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(900);
    await shot('mobile-4-customer-order-detail.png');
  } else {
    console.log('no customer order card found');
  }

  // 5. 客户「我的」
  await page.goto(`${BASE}/m/customer/mine`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(700);
  await shot('mobile-5-customer-mine.png');

  // 切换配送员
  await page.evaluate(() => localStorage.clear());
  await login('delivery01');
  console.log('after courier login url=' + page.url());
  await shot('mobile-6-courier-tasks.png');

  // 6. 配送员任务详情
  const firstTask = page.locator('.m-task').first();
  if (await firstTask.count()) {
    await firstTask.click();
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(900);
    await shot('mobile-7-courier-task-detail.png');
  } else {
    console.log('no courier task card found');
  }

  // 7. 配送员「我的」
  await page.goto(`${BASE}/m/courier/mine`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(700);
  await shot('mobile-8-courier-mine.png');

  console.log('console errors: ' + JSON.stringify(errors.slice(0, 20), null, 2));
  await browser.close();
})().catch((e) => {
  console.error('SCRIPT FAILED', e);
  process.exit(1);
});
