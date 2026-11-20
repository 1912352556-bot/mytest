const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
        AlignmentType, BorderStyle, WidthType, ShadingType, HeadingLevel } = require('docx');
const fs = require('fs');

// 边框样式
const border = { style: BorderStyle.SINGLE, size: 1, color: "666666" };
const borders = { top: border, bottom: border, left: border, right: border };

// 表头样式
const headerShading = { fill: "4472C4", type: ShadingType.CLEAR };
// 交替行颜色（都设为白色）
const rowShading1 = { fill: "FFFFFF", type: ShadingType.CLEAR };
const rowShading2 = { fill: "FFFFFF", type: ShadingType.CLEAR };

// 创建表头单元格
function createHeaderCell(text, width) {
    return new TableCell({
        borders,
        width: { size: width, type: WidthType.DXA },
        shading: headerShading,
        margins: { top: 80, bottom: 80, left: 100, right: 100 },
        children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ text, bold: true, color: "FFFFFF", font: "宋体", size: 22 })]
        })]
    });
}

// 创建数据单元格
function createDataCell(text, width, isAlternate) {
    return new TableCell({
        borders,
        width: { size: width, type: WidthType.DXA },
        shading: isAlternate ? rowShading2 : rowShading1,
        margins: { top: 60, bottom: 60, left: 100, right: 100 },
        children: [new Paragraph({
            children: [new TextRun({ text, font: "宋体", size: 20 })]
        })]
    });
}

// 创建方法单元格
function createMethodCell(method, width, isAlternate) {
    // 方法颜色
    let color = "333333";
    let bgFill = isAlternate ? rowShading2.fill : rowShading1.fill;
    if (method === "GET") color = "2E75B6";
    else if (method === "POST") color = "538135";
    else if (method === "PUT") color = "C65911";
    else if (method === "DELETE") color = "C00000";
    
    return new TableCell({
        borders,
        width: { size: width, type: WidthType.DXA },
        shading: { fill: bgFill, type: ShadingType.CLEAR },
        margins: { top: 60, bottom: 60, left: 100, right: 100 },
        children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ text: method, bold: true, color, font: "宋体", size: 20 })]
        })]
    });
}

// 创建Controller表格
function createControllerTable(controllerName, interfaces, totalWidth) {
    const rows = [];
    
    // 表头行
    rows.push(new TableRow({
        children: [
            createHeaderCell("方法", 900),
            createHeaderCell("路径", 3800),
            createHeaderCell("功能", totalWidth - 4700)
        ]
    }));
    
    // 数据行
    interfaces.forEach((item, index) => {
        const isAlternate = index % 2 === 1;
        rows.push(new TableRow({
            children: [
                createMethodCell(item.method, 900, isAlternate),
                createDataCell(item.path, 3800, isAlternate),
                createDataCell(item.desc, totalWidth - 4700, isAlternate)
            ]
        }));
    });
    
    return new Table({
        width: { size: totalWidth, type: WidthType.DXA },
        columnWidths: [900, 3800, totalWidth - 4700],
        rows: rows
    });
}

// 接口数据
const controllers = [
    {
        name: "1. 用户管理 /user",
        interfaces: [
            { method: "POST", path: "/user/login", desc: "用户登录" },
            { method: "POST", path: "/user/register", desc: "用户注册" },
            { method: "POST", path: "/user/logout", desc: "退出登录" },
            { method: "GET", path: "/user/info", desc: "获取当前用户信息" },
            { method: "PUT", path: "/user/password", desc: "修改密码" },
            { method: "GET", path: "/user/list", desc: "获取用户列表（分页+搜索）" },
            { method: "POST", path: "/user", desc: "新增用户" },
            { method: "PUT", path: "/user", desc: "更新用户" },
            { method: "DELETE", path: "/user/{id}", desc: "删除用户" },
            { method: "PUT", path: "/user/{id}/password/reset", desc: "重置用户密码" },
            { method: "PUT", path: "/user/{id}/status/{status}", desc: "修改用户状态" }
        ]
    },
    {
        name: "2. 生鲜商品管理 /dish",
        interfaces: [
            { method: "GET", path: "/dish/list", desc: "获取商品列表（分页+搜索）" },
            { method: "GET", path: "/dish/{id}", desc: "获取商品详情" },
            { method: "POST", path: "/dish", desc: "新增商品（自动创建库存记录）" },
            { method: "PUT", path: "/dish", desc: "更新商品" },
            { method: "DELETE", path: "/dish/{id}", desc: "删除商品" },
            { method: "PUT", path: "/dish/{id}/status/{status}", desc: "修改商品状态" },
            { method: "GET", path: "/dish/active", desc: "获取有效商品列表（下单用）" },
            { method: "GET", path: "/dish/category/list", desc: "获取商品分类列表" },
            { method: "POST", path: "/dish/category", desc: "新增商品分类" },
            { method: "PUT", path: "/dish/category", desc: "更新商品分类" },
            { method: "DELETE", path: "/dish/category/{id}", desc: "删除商品分类" }
        ]
    },
    {
        name: "3. 订单管理 /order",
        interfaces: [
            { method: "GET", path: "/order/list", desc: "获取订单列表（分页+搜索）" },
            { method: "GET", path: "/order/{id}", desc: "获取订单详情" },
            { method: "POST", path: "/order", desc: "创建订单（扣减库存）" },
            { method: "PUT", path: "/order", desc: "更新订单" },
            { method: "DELETE", path: "/order/{id}", desc: "删除订单" },
            { method: "PUT", path: "/order/status", desc: "更新订单状态" },
            { method: "PUT", path: "/order/{id}/cancel", desc: "取消订单（补回库存）" },
            { method: "POST", path: "/order/import", desc: "批量导入订单" },
            { method: "GET", path: "/order/export", desc: "导出订单" },
            { method: "GET", path: "/order/timeout", desc: "获取超时未调度订单" },
            { method: "GET", path: "/order/pending-schedule", desc: "获取待调度订单列表" }
        ]
    },
    {
        name: "4. 调度管理 /schedule",
        interfaces: [
            { method: "GET", path: "/schedule/list", desc: "获取调度任务列表（分页+搜索）" },
            { method: "GET", path: "/schedule/{id}", desc: "获取调度任务详情" },
            { method: "POST", path: "/schedule", desc: "创建调度任务" },
            { method: "POST", path: "/schedule/batch", desc: "批量创建调度任务" },
            { method: "POST", path: "/schedule/auto-assign", desc: "自动分配调度任务" },
            { method: "PUT", path: "/schedule/assign", desc: "手动分配任务（核心，创建配送任务）" },
            { method: "PUT", path: "/schedule/{id}/status/{status}", desc: "更新任务状态" },
            { method: "PUT", path: "/schedule/{id}/cancel", desc: "取消调度任务" },
            { method: "GET", path: "/schedule/{taskId}/logs", desc: "获取调度日志" },
            { method: "GET", path: "/schedule/pending-assign", desc: "获取待分配任务列表" },
            { method: "GET", path: "/schedule/available-persons", desc: "获取可分配的配送人员" }
        ]
    },
    {
        name: "5. 配送管理 /delivery",
        interfaces: [
            { method: "GET", path: "/delivery/person/list", desc: "获取配送人员列表" },
            { method: "GET", path: "/delivery/person/{id}", desc: "获取配送人员详情" },
            { method: "POST", path: "/delivery/person", desc: "新增配送人员" },
            { method: "PUT", path: "/delivery/person", desc: "更新配送人员" },
            { method: "DELETE", path: "/delivery/person/{id}", desc: "删除配送人员" },
            { method: "PUT", path: "/delivery/person/{id}/status/{status}", desc: "修改配送人员状态" },
            { method: "GET", path: "/delivery/person/idle", desc: "获取空闲配送人员" },
            { method: "GET", path: "/delivery/task/list", desc: "获取配送任务列表" },
            { method: "GET", path: "/delivery/task/{id}", desc: "获取配送任务详情" },
            { method: "POST", path: "/delivery/task", desc: "创建配送任务" },
            { method: "PUT", path: "/delivery/task/{id}/start", desc: "开始配送（设置取货时间）" },
            { method: "PUT", path: "/delivery/task/complete", desc: "完成配送（核心，状态联动）" },
            { method: "PUT", path: "/delivery/task/{id}/exception", desc: "异常上报" },
            { method: "GET", path: "/delivery/task/{taskId}/track", desc: "获取配送轨迹" },
            { method: "POST", path: "/delivery/task/location", desc: "上报配送位置" }
        ]
    },
    {
        name: "6. 库存管理 /inventory",
        interfaces: [
            { method: "GET", path: "/inventory/list", desc: "获取库存列表（分页）" },
            { method: "GET", path: "/inventory/{id}", desc: "获取库存详情" },
            { method: "POST", path: "/inventory", desc: "新增库存记录" },
            { method: "PUT", path: "/inventory", desc: "更新库存信息" },
            { method: "DELETE", path: "/inventory/{id}", desc: "删除库存记录" },
            { method: "POST", path: "/inventory/stock-in", desc: "入库操作（记录日志）" },
            { method: "POST", path: "/inventory/stock-out", desc: "出库操作（记录日志）" },
            { method: "GET", path: "/inventory/{inventoryId}/records", desc: "获取库存变更记录" },
            { method: "GET", path: "/inventory/warning", desc: "获取库存预警列表" },
            { method: "PUT", path: "/inventory/{id}/threshold/{threshold}", desc: "设置预警阈值" },
            { method: "PUT", path: "/inventory/threshold/batch", desc: "批量设置预警阈值" }
        ]
    },
    {
        name: "7. 统计分析 /statistics",
        interfaces: [
            { method: "GET", path: "/statistics/overview", desc: "获取首页统计概览" },
            { method: "GET", path: "/statistics/order", desc: "获取订单统计（按日期范围）" },
            { method: "GET", path: "/statistics/schedule", desc: "获取调度统计" },
            { method: "GET", path: "/statistics/inventory-turnover", desc: "获取库存周转统计" },
            { method: "GET", path: "/statistics/order/daily", desc: "获取每日订单量统计" },
            { method: "GET", path: "/statistics/order/monthly", desc: "获取每月订单量统计" },
            { method: "GET", path: "/statistics/order/status-distribution", desc: "获取订单状态分布" },
            { method: "GET", path: "/statistics/order/area-distribution", desc: "获取配送区域订单分布" },
            { method: "GET", path: "/statistics/dish/hot-ranking", desc: "获取热销商品排行" },
            { method: "GET", path: "/statistics/delivery/performance", desc: "获取配送人员绩效统计" },
            { method: "GET", path: "/statistics/inventory", desc: "获取库存统计" }
        ]
    },
    {
        name: "8. 数据字典 /dict",
        interfaces: [
            { method: "GET", path: "/dict/type/list", desc: "获取字典类型列表" },
            { method: "GET", path: "/dict/type/{id}", desc: "获取字典类型详情" },
            { method: "POST", path: "/dict/type", desc: "新增字典类型" },
            { method: "PUT", path: "/dict/type", desc: "更新字典类型" },
            { method: "DELETE", path: "/dict/type/{id}", desc: "删除字典类型" },
            { method: "GET", path: "/dict/data/list/{dictTypeId}", desc: "获取字典数据列表" },
            { method: "GET", path: "/dict/data/code/{dictCode}", desc: "根据编码获取字典数据" },
            { method: "POST", path: "/dict/data", desc: "新增字典数据" },
            { method: "PUT", path: "/dict/data", desc: "更新字典数据" },
            { method: "DELETE", path: "/dict/data/{id}", desc: "删除字典数据" }
        ]
    },
    {
        name: "9. 文件管理 /file",
        interfaces: [
            { method: "POST", path: "/file/upload", desc: "上传图片" },
            { method: "GET", path: "/file/image/{filename}", desc: "访问图片" },
            { method: "DELETE", path: "/file/image/{filename}", desc: "删除图片" }
        ]
    }
];

// 总宽度（A4纸，1英寸边距）
const TOTAL_WIDTH = 9026;

// 创建文档内容
const children = [];

// 标题
children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 300 },
    children: [new TextRun({ text: "生鲜即时配送调度系统 - 后端接口汇总", bold: true, font: "宋体", size: 36 })]
}));

// 统计信息
const totalInterfaces = controllers.reduce((sum, c) => sum + c.interfaces.length, 0);
children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 400 },
    children: [new TextRun({ text: `共 ${controllers.length} 个 Controller，${totalInterfaces} 个接口`, font: "宋体", size: 24, color: "666666" })]
}));

// 每个Controller一张表格
controllers.forEach((controller, index) => {
    // Controller名称
    children.push(new Paragraph({
        spacing: { before: 300, after: 150 },
        children: [new TextRun({ text: controller.name, bold: true, font: "宋体", size: 26, color: "333333" })]
    }));
    
    // 表格
    children.push(createControllerTable(controller.name, controller.interfaces, TOTAL_WIDTH));
    
    // 空行
    children.push(new Paragraph({ spacing: { after: 100 }, children: [] }));
});

// 创建文档
const doc = new Document({
    sections: [{
        properties: {
            page: {
                size: { width: 11906, height: 16838 },
                margin: { top: 1134, right: 1134, bottom: 1134, left: 1134 }
            }
        },
        children: children
    }]
});

// 生成文件
Packer.toBuffer(doc).then(buffer => {
    const outputPath = "d:/myproject/wfxyend/a-a-a/word/后端接口汇总_v2.docx";
    fs.writeFileSync(outputPath, buffer);
    console.log(`文档已生成: ${outputPath}`);
    console.log(`共 ${controllers.length} 个 Controller，${totalInterfaces} 个接口`);
});
