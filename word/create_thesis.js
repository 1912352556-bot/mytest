const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
        Header, Footer, AlignmentType, LevelFormat, PageNumber,
        HeadingLevel, BorderStyle, WidthType, ShadingType, LineRuleType } = require('docx');
const fs = require('fs');

// 边框样式
const border = { style: BorderStyle.SINGLE, size: 1, color: "000000" };
const borders = { top: border, bottom: border, left: border, right: border };

// 单元格样式
const cellBorders = { top: { style: BorderStyle.SINGLE, size: 1, color: "000000" },
                      bottom: { style: BorderStyle.SINGLE, size: 1, color: "000000" },
                      left: { style: BorderStyle.SINGLE, size: 1, color: "000000" },
                      right: { style: BorderStyle.SINGLE, size: 1, color: "000000" } };

// 创建表格单元格
function createCell(text, width, isBold = false, shading = null) {
    const cellProps = {
        borders: cellBorders,
        width: { size: width, type: WidthType.DXA },
        margins: { top: 80, bottom: 80, left: 120, right: 120 },
        children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ text, font: "宋体", size: 24, bold: isBold })]
        })]
    };
    if (shading) {
        cellProps.shading = { fill: shading, type: ShadingType.CLEAR };
    }
    return new TableCell(cellProps);
}

// 创建段落
function createParagraph(text, options = {}) {
    const { font = "宋体", size = 24, bold = false, alignment = AlignmentType.JUSTIFIED, firstLine = 0, spacing = 300 } = options;
    return new Paragraph({
        alignment,
        spacing: { line: spacing, lineRule: "auto" },
        indent: firstLine > 0 ? { firstLine } : undefined,
        children: [new TextRun({ text, font, size, bold })]
    });
}

// 创建标题段落
function createTitle(text, level = 1) {
    const sizes = { 1: 32, 2: 28, 3: 26 };
    return new Paragraph({
        heading: level === 1 ? HeadingLevel.HEADING_1 : (level === 2 ? HeadingLevel.HEADING_2 : HeadingLevel.HEADING_3),
        spacing: { before: 240, after: 180 },
        children: [new TextRun({ text, font: "黑体", size: sizes[level] || 24, bold: true })]
    });
}

const doc = new Document({
    styles: {
        default: {
            document: {
                run: { font: "宋体", size: 24 }
            }
        },
        paragraphStyles: [
            { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
              run: { size: 32, bold: true, font: "黑体" },
              paragraph: { spacing: { before: 240, after: 180 }, outlineLevel: 0 } },
            { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
              run: { size: 28, bold: true, font: "黑体" },
              paragraph: { spacing: { before: 200, after: 160 }, outlineLevel: 1 } },
            { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
              run: { size: 24, bold: true, font: "黑体" },
              paragraph: { spacing: { before: 160, after: 120 }, outlineLevel: 2 } },
        ]
    },
    numbering: {
        config: [
            { reference: "bullets",
              levels: [{ level: 0, format: LevelFormat.BULLET, text: "\u2022", alignment: AlignmentType.LEFT,
                style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
            { reference: "numbers",
              levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
                style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
        ]
    },
    sections: [{
        properties: {
            page: {
                size: { width: 11906, height: 16838 },
                margin: { top: 1701, right: 1134, bottom: 1134, left: 1701 }
            }
        },
        headers: {
            default: new Header({
                children: [new Paragraph({
                    alignment: AlignmentType.RIGHT,
                    children: [new TextRun({ text: "生鲜即时配送调度系统", font: "宋体", size: 18, color: "666666" })]
                })]
            })
        },
        footers: {
            default: new Footer({
                children: [new Paragraph({
                    alignment: AlignmentType.CENTER,
                    children: [
                        new TextRun({ text: "第 ", font: "宋体", size: 21 }),
                        new TextRun({ children: [PageNumber.CURRENT], font: "宋体", size: 21 }),
                        new TextRun({ text: " 页", font: "宋体", size: 21 })
                    ]
                })]
            })
        },
        children: [
            // ============ 封面 ============
            new Paragraph({ spacing: { line: 600 }, children: [] }),
            new Paragraph({
                alignment: AlignmentType.CENTER,
                spacing: { line: 360 },
                children: [new TextRun({ text: "本科毕业设计技术报告", font: "黑体", size: 44, bold: true })]
            }),
            new Paragraph({ spacing: { line: 600 }, children: [] }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 200 },
                children: [new TextRun({ text: "课题名称：", font: "黑体", size: 32 })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 400 },
                children: [new TextRun({ text: "生鲜即时配送调度系统", font: "黑体", size: 36, underline: {} })]
            }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 200 },
                children: [new TextRun({ text: "学    院：", font: "黑体", size: 32 })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 400 },
                children: [new TextRun({ text: "计算机工程学院", font: "黑体", size: 32, underline: {} })]
            }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 200 },
                children: [new TextRun({ text: "学生姓名：", font: "黑体", size: 32 })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 400 },
                children: [new TextRun({ text: "                    ", font: "黑体", size: 32, underline: {} })]
            }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 200 },
                children: [new TextRun({ text: "学    号：", font: "黑体", size: 32 })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 400 },
                children: [new TextRun({ text: "                    ", font: "黑体", size: 32, underline: {} })]
            }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 200 },
                children: [new TextRun({ text: "专    业：", font: "黑体", size: 32 })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 400 },
                children: [new TextRun({ text: "                          ", font: "黑体", size: 32, underline: {} })]
            }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 200 },
                children: [new TextRun({ text: "班    级：", font: "黑体", size: 32 })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 400 },
                children: [new TextRun({ text: "                    ", font: "黑体", size: 32, underline: {} })]
            }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 200 },
                children: [new TextRun({ text: "指导教师：", font: "黑体", size: 32 })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                indent: { firstLine: 400 },
                children: [new TextRun({ text: "                    ", font: "黑体", size: 32, underline: {} })]
            }),
            new Paragraph({ spacing: { line: 600 }, children: [] }),
            new Paragraph({
                alignment: AlignmentType.CENTER,
                spacing: { line: 360 },
                children: [new TextRun({ text: "2026年5月", font: "黑体", size: 32 })]
            }),

            // ============ 原创性声明 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            new Paragraph({
                alignment: AlignmentType.CENTER,
                spacing: { before: 300, after: 300 },
                children: [new TextRun({ text: "原创性声明", font: "黑体", size: 32, bold: true })]
            }),
            createParagraph("本人郑重声明：本人所呈交的学位论文是在指导老师的指导下独立进行研究所取得的成果。除文中已经注明引用的内容外，本论文不包含任何其他个人或集体已经发表或撰写过的科研成果。对本文的研究做出重要贡献的个人和集体，均已在文中以明确方式标明。本人已完全意识到本声明的法律责任由本人承担。", { firstLine: 480, size: 21 }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            createParagraph("论文作者签名：                              日    期：       年     月     日", { firstLine: 3000, size: 21 }),

            // ============ 版权使用授权书 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            new Paragraph({
                alignment: AlignmentType.CENTER,
                spacing: { before: 300, after: 300 },
                children: [new TextRun({ text: "学位论文版权使用授权书", font: "黑体", size: 32, bold: true })]
            }),
            createParagraph("本人完全了解学校有关保留、使用学位论文的规定，同意学校保留或向国家有关部门或机构送交论文的复印件和电子版，允许论文被查阅和借阅；本人授权学校可以将本学位论文的全部或部分内容编入有关数据库进行检索，可以采用影印、缩印或其他复制手段保存和汇编本学位论文。", { firstLine: 480, size: 21 }),
            createParagraph("(保密论文在解密后应遵守此规定)", { firstLine: 480, size: 21 }),
            new Paragraph({ spacing: { line: 400 }, children: [] }),
            createParagraph("论文作者签名：                              指导教师签名：", { firstLine: 480, size: 21 }),
            new Paragraph({ spacing: { line: 200 }, children: [] }),
            createParagraph("日    期：       年     月     日                    日    期：       年     月     日", { firstLine: 480, size: 21 }),

            // ============ 摘要 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            new Paragraph({
                alignment: AlignmentType.CENTER,
                spacing: { before: 300, after: 300 },
                children: [new TextRun({ text: "摘要", font: "黑体", size: 32, bold: true })]
            }),
            createParagraph({
                alignment: AlignmentType.JUSTIFIED,
                spacing: { line: 332, lineRule: LineRuleType.AUTO, before: 0, after: 0 },
                indentations: { firstLine: 480 },
                children: [new TextRun({ text: "本论文针对中小型生鲜零售与前置仓企业在即时配送调度方面的实际需求，设计并实现了一套完整的生鲜即时配送调度系统。该系统采用前后端分离的架构设计，后端基于JDK 17和Spring Boot 3.2框架开发，持久层使用MyBatis-Plus与MySQL 8.0数据库进行数据交互；前端采用Vue 3和Element Plus组件库构建响应式管理界面。", font: "宋体", size: 24 })]
            }),
            createParagraph({
                alignment: AlignmentType.JUSTIFIED,
                spacing: { line: 332, lineRule: LineRuleType.AUTO, before: 0, after: 0 },
                indentations: { firstLine: 480 },
                children: [new TextRun({ text: "在功能实现方面，系统涵盖六大核心模块：用户权限管理模块实现了管理员、调度员、配送员三级角色的JWT鉴权机制；订单管理模块支持订单的创建、查询、状态更新及超时预警功能；生产调度模块提供了基于配送区域和时间的自动分配调度任务功能；物流配送模块实现了配送人员管理、任务指派及配送轨迹记录；库存管理模块完成了生鲜商品库存查询、出库扣减及阈值预警；统计分析模块提供了订单量统计、调度完成率及库存周转率等数据报表。", font: "宋体", size: 24 })]
            }),
            createParagraph({
                alignment: AlignmentType.JUSTIFIED,
                spacing: { line: 332, lineRule: LineRuleType.AUTO, before: 0, after: 0 },
                indentations: { firstLine: 480 },
                children: [new TextRun({ text: "系统通过功能测试验证，接口符合RESTful规范，各模块功能运行稳定。测试结果表明，该系统能够有效提升生鲜零售企业的订单处理效率和配送调度合理性，为中小型生鲜前置仓企业的数字化调度管理提供了可行的技术解决方案。", font: "宋体", size: 24 })]
            }),
            new Paragraph({ spacing: { line: 300 }, children: [] }),
            createParagraph("关键词：生鲜即时配送调度系统；Spring Boot；Vue 3；MyBatis-Plus；订单管理", { firstLine: 0, size: 24 }),

            // ============ Abstract ============
            new Paragraph({
                alignment: AlignmentType.CENTER,
                spacing: { before: 300, after: 300 },
                children: [new TextRun({ text: "Abstract", font: "Times New Roman", size: 32, bold: true })]
            }),
            createParagraph("With the acceleration of people's life pace and consumption upgrade, the prefabricated dish industry has ushered in rapid development opportunities. Prefabricated dish enterprises are facing challenges such as low order processing efficiency, unreasonable delivery scheduling, and chaotic inventory management. Traditional manual scheduling methods are not only inefficient but also difficult to achieve delivery route optimization and rational resource allocation. To address the above issues, this paper designs and implements a prefabricated dish scheduling management system based on Spring Boot and Vue.", { firstLine: 480, size: 22 }),
            createParagraph("The system adopts a front-end and back-end separation architecture. The back-end implements business services and data persistence based on technologies such as Spring Boot, MyBatis-Plus, and MySQL. The front-end builds a visual management interface based on Vue3, TypeScript, Element Plus, and ECharts. The system integrates the Gaode Map API to achieve precise positioning of delivery addresses and real-time tracking of delivery trajectories.", { firstLine: 480, size: 22 }),
            createParagraph("The system focuses on the daily operation process of prefabricated dish enterprises and designs core functional modules including dish management, order management, inventory management, delivery scheduling, delivery trajectory tracking, data dictionary, and system management. Through the modeling of business objects such as dish categories, order details, inventory records, delivery personnel, delivery tasks, and delivery trajectories, the system achieves unified management and visual display of enterprise operation data.", { firstLine: 480, size: 22 }),
            createParagraph("Practice shows that this system can effectively improve the order processing efficiency and delivery scheduling rationality of prefabricated dish enterprises, providing a feasible solution for the informatization construction of small and medium-sized prefabricated dish enterprises.", { firstLine: 480, size: 22 }),
            new Paragraph({ spacing: { line: 300 }, children: [] }),
            createParagraph("Keywords: Prefabricated Dish; Order Management; Delivery Scheduling; Inventory Management; Spring Boot; Vue", { firstLine: 0, size: 22, bold: true }),

            // ============ 目录 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            new Paragraph({
                alignment: AlignmentType.CENTER,
                spacing: { before: 300, after: 300 },
                children: [new TextRun({ text: "目录", font: "黑体", size: 32, bold: true })]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "第1章  绪论", font: "黑体", size: 28 }),
                    new TextRun({ text: "        1", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    1.1  研究背景与意义", font: "宋体", size: 24 }),
                    new TextRun({ text: "        1", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    1.2  国内外研究现状", font: "宋体", size: 24 }),
                    new TextRun({ text: "        2", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    1.3  主要研究内容", font: "宋体", size: 24 }),
                    new TextRun({ text: "        3", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "第2章  系统需求分析", font: "黑体", size: 28 }),
                    new TextRun({ text: "        4", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    2.1  功能性需求分析", font: "宋体", size: 24 }),
                    new TextRun({ text: "        4", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    2.2  非功能性需求分析", font: "宋体", size: 24 }),
                    new TextRun({ text: "        6", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "第3章  系统设计", font: "黑体", size: 28 }),
                    new TextRun({ text: "        7", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    3.1  系统架构设计", font: "宋体", size: 24 }),
                    new TextRun({ text: "        7", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    3.2  数据库设计", font: "宋体", size: 24 }),
                    new TextRun({ text: "        8", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    3.3  功能模块设计", font: "宋体", size: 24 }),
                    new TextRun({ text: "        12", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "第4章  系统实现", font: "黑体", size: 28 }),
                    new TextRun({ text: "        14", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    4.1  开发环境与工具", font: "宋体", size: 24 }),
                    new TextRun({ text: "        14", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    4.2  核心功能实现", font: "宋体", size: 24 }),
                    new TextRun({ text: "        15", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    4.3  系统界面展示", font: "宋体", size: 24 }),
                    new TextRun({ text: "        18", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "第5章  总结与展望", font: "黑体", size: 28 }),
                    new TextRun({ text: "        20", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    5.1  工作总结", font: "宋体", size: 24 }),
                    new TextRun({ text: "        20", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "    5.2  未来展望", font: "宋体", size: 24 }),
                    new TextRun({ text: "        21", font: "宋体", size: 24 })
                ]
            }),
            new Paragraph({
                spacing: { line: 360 },
                children: [
                    new TextRun({ text: "参考文献", font: "黑体", size: 28 }),
                    new TextRun({ text: "        22", font: "宋体", size: 24 })
                ]
            }),

            // ============ 第1章 绪论 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            createTitle("第1章  绪论", 1),
            createTitle("1.1  研究背景与意义", 2),
            createParagraph("近年来，随着即时零售与前置仓模式的兴起，生鲜即时配送行业迎来了快速发展。生鲜商品是指蔬菜、水果、肉禽蛋、水产海鲜、乳品烘焙等与居民日常饮食密切相关的食用农产品，具有保质期短、易损耗、储存温层要求高等特点。随着“线上下单、30分钟至1小时送达”成为主流消费习惯，生鲜即时配送在城市家庭日常采购中的渗透率持续提升。", { firstLine: 480 }),
            createParagraph("生鲜零售企业面临着订单波峰集中、配送时效要求高、短保商品损耗大等问题。传统的人工调度方式不仅效率低下，而且难以兼顾配送时效与冷链温层要求，容易造成超时送达和商品损耗。如何提高订单处理效率、优化配送调度、降低运营与损耗成本成为生鲜零售企业急需解决的问题。", { firstLine: 480 }),
            createParagraph("本课题设计并实现一套生鲜即时配送调度系统，旨在帮助生鲜零售与前置仓企业实现订单管理、配送调度、库存管理的数字化和智能化，提高运营效率，降低损耗与运营成本。", { firstLine: 480 }),
            createTitle("1.2  国内外研究现状", 2),
            createParagraph("在餐饮信息化领域，国内外已有不少成熟的管理系统。国外的Sysco、US Foods等大型食材供应链企业已经实现了全流程的数字化管理，能够实时追踪订单状态、优化配送路线、管理库存水平。国内的美团、饿了么等外卖平台也在配送调度方面积累了丰富的经验。", { firstLine: 480 }),
            createParagraph("然而，专门针对生鲜即时配送场景的调度管理系统相对较少。现有的一些餐饮或电商后台系统虽然功能全面，但往往缺乏针对生鲜商品特点的专门设计，如波峰订单处理、冷链温层调度、短保商品损耗管理等。因此，开发一套适合生鲜即时配送特点的调度管理系统具有重要的实用价值。", { firstLine: 480 }),
            createTitle("1.3  主要研究内容", 2),
            createParagraph("本课题的主要研究内容包括：", { firstLine: 0 }),
            createParagraph("（1）系统需求分析：对生鲜即时配送调度系统的功能需求和非功能需求进行详细分析。", { firstLine: 480 }),
            createParagraph("（2）系统设计：设计系统的整体架构、数据库结构和功能模块。", { firstLine: 480 }),
            createParagraph("（3）系统实现：采用Spring Boot和Vue技术栈实现系统的各个功能模块。", { firstLine: 480 }),
            createParagraph("（4）系统测试：对系统进行功能测试和性能测试，确保系统稳定运行。", { firstLine: 480 }),

            // ============ 第2章 系统需求分析 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            createTitle("第2章  系统需求分析", 1),
            createTitle("2.1  功能性需求分析", 2),
            createTitle("2.1.1  生鲜商品管理", 3),
            createParagraph("生鲜商品管理模块主要用于维护蔬菜、水果、肉禽蛋、水产海鲜、乳品烘焙、粮油调味等生鲜商品信息，包括商品名称、商品编码、分类、单价、单位、规格、保质期、储存温层、产地、损耗率、描述与图片等。系统支持商品的增删改查、按分类与温层筛选，以及商品的上下架管理。", { firstLine: 480 }),
            createTitle("2.1.2  订单管理", 3),
            createParagraph("订单管理模块用于处理客户订单，包括创建订单、查询订单、取消订单、查看订单详情等功能。订单包含客户信息、配送地址、配送时间、订单明细等信息。系统支持订单状态的流转管理。", { firstLine: 480 }),
            createTitle("2.1.3  库存管理", 3),
            createParagraph("库存管理模块用于管理生鲜商品库存，包括库存查询、库存预警、库存入库、库存出库等功能。系统支持设置库存预警阈值，当库存低于阈值时自动提醒。库存变更记录完整，可追溯每次变更的详细情况。", { firstLine: 480 }),
            createTitle("2.1.4  配送调度", 3),
            createParagraph("配送调度模块用于管理配送人员和配送任务。系统支持配送人员信息管理、配送任务分配、任务状态跟踪等功能。调度员可以根据订单的配送地址和配送时间，合理分配配送任务。", { firstLine: 480 }),
            createTitle("2.1.5  配送轨迹", 3),
            createParagraph("配送轨迹模块用于记录和展示配送过程。通过集成高德地图API，系统可以记录配送人员的位置轨迹，并在地图上展示配送路线，便于监控配送进度和处理异常情况。", { firstLine: 480 }),
            createTitle("2.1.6  系统管理", 3),
            createParagraph("系统管理模块用于管理用户信息、角色权限、数据字典等。系统支持多角色管理，包括管理员、调度员、配送员等不同角色的权限控制。", { firstLine: 480 }),
            createTitle("2.2  非功能性需求分析", 2),
            createParagraph("（1）性能需求：系统应能够支持多用户并发访问，响应时间控制在合理范围内。", { firstLine: 480 }),
            createParagraph("（2）安全性需求：系统应采用安全的认证机制，保护用户数据和业务数据的安全。", { firstLine: 480 }),
            createParagraph("（3）可用性需求：系统应具有友好的用户界面，操作简便，易于上手。", { firstLine: 480 }),
            createParagraph("（4）可扩展性需求：系统应具有良好的架构设计，便于后期功能扩展和维护。", { firstLine: 480 }),

            // ============ 第3章 系统设计 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            createTitle("第3章  系统设计", 1),
            createTitle("3.1  系统架构设计", 2),
            createParagraph("系统采用前后端分离架构，前端使用Vue3框架构建单页面应用，后端使用Spring Boot框架构建RESTful API服务。", { firstLine: 480 }),
            createParagraph("前端技术栈包括Vue3、TypeScript、Element Plus、Vite等。前端通过HTTP请求与后端API进行交互，使用Vue Router实现页面路由管理，使用Pinia进行状态管理。", { firstLine: 480 }),
            createParagraph("后端技术栈包括Spring Boot、MyBatis-Plus、MySQL、Redis等。后端提供RESTful API接口，使用JWT实现用户认证和授权。", { firstLine: 480 }),
            createTitle("3.2  数据库设计", 2),
            createParagraph("系统数据库采用MySQL，主要包含以下数据表：", { firstLine: 480 }),

            // 数据库表设计
            new Paragraph({
                spacing: { before: 200, after: 100 },
                children: [new TextRun({ text: "表3-1  用户表（t_user）", font: "黑体", size: 24, bold: true })]
            }),
            new Table({
                width: { size: 8500, type: WidthType.DXA },
                columnWidths: [2000, 1500, 2000, 1500, 1500],
                rows: [
                    new TableRow({ children: [
                        createCell("字段名", 2000, true, "D9D9D9"),
                        createCell("数据类型", 1500, true, "D9D9D9"),
                        createCell("约束", 2000, true, "D9D9D9"),
                        createCell("允许空", 1500, true, "D9D9D9"),
                        createCell("说明", 1500, true, "D9D9D9")
                    ]}),
                    new TableRow({ children: [
                        createCell("id", 2000), createCell("BIGINT", 1500),
                        createCell("PRIMARY KEY", 2000), createCell("否", 1500), createCell("主键ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("username", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("UNIQUE", 2000), createCell("否", 1500), createCell("用户名", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("password", 2000), createCell("VARCHAR(100)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("密码", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("realname", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("真实姓名", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("phone", 2000), createCell("VARCHAR(20)", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("手机号", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("role", 2000), createCell("TINYINT", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("角色", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("status", 2000), createCell("TINYINT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("状态", 1500)
                    ]}),
                ]
            }),

            new Paragraph({ spacing: { line: 300 }, children: [] }),
            new Paragraph({
                spacing: { before: 200, after: 100 },
                children: [new TextRun({ text: "表3-2  生鲜商品表（t_dish）", font: "黑体", size: 24, bold: true })]
            }),
            new Table({
                width: { size: 8500, type: WidthType.DXA },
                columnWidths: [2000, 1500, 2000, 1500, 1500],
                rows: [
                    new TableRow({ children: [
                        createCell("字段名", 2000, true, "D9D9D9"),
                        createCell("数据类型", 1500, true, "D9D9D9"),
                        createCell("约束", 2000, true, "D9D9D9"),
                        createCell("允许空", 1500, true, "D9D9D9"),
                        createCell("说明", 1500, true, "D9D9D9")
                    ]}),
                    new TableRow({ children: [
                        createCell("id", 2000), createCell("BIGINT", 1500),
                        createCell("PRIMARY KEY", 2000), createCell("否", 1500), createCell("主键ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("dish_name", 2000), createCell("VARCHAR(100)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("生鲜商品名称", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("dish_code", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("UNIQUE", 2000), createCell("否", 1500), createCell("生鲜商品编码", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("category_id", 2000), createCell("BIGINT", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("分类ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("price", 2000), createCell("DECIMAL(10,2)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("单价", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("stock", 2000), createCell("INT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("当前库存", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("spec", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("规格（如500g/份）", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("shelf_life_days", 2000), createCell("INT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("保质期（天）", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("storage_type", 2000), createCell("TINYINT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("储存温层", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("origin", 2000), createCell("VARCHAR(100)", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("产地", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("loss_rate", 2000), createCell("DECIMAL(5,2)", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("损耗率（%）", 1500)
                    ]}),
                ]
            }),

            new Paragraph({ spacing: { line: 300 }, children: [] }),
            new Paragraph({
                spacing: { before: 200, after: 100 },
                children: [new TextRun({ text: "表3-3  订单表（t_order）", font: "黑体", size: 24, bold: true })]
            }),
            new Table({
                width: { size: 8500, type: WidthType.DXA },
                columnWidths: [2000, 1500, 2000, 1500, 1500],
                rows: [
                    new TableRow({ children: [
                        createCell("字段名", 2000, true, "D9D9D9"),
                        createCell("数据类型", 1500, true, "D9D9D9"),
                        createCell("约束", 2000, true, "D9D9D9"),
                        createCell("允许空", 1500, true, "D9D9D9"),
                        createCell("说明", 1500, true, "D9D9D9")
                    ]}),
                    new TableRow({ children: [
                        createCell("id", 2000), createCell("BIGINT", 1500),
                        createCell("PRIMARY KEY", 2000), createCell("否", 1500), createCell("主键ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("order_no", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("UNIQUE", 2000), createCell("否", 1500), createCell("订单编号", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("customer_name", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("客户姓名", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("customer_phone", 2000), createCell("VARCHAR(20)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("客户电话", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("delivery_address", 2000), createCell("VARCHAR(255)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("配送地址", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("total_amount", 2000), createCell("DECIMAL(10,2)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("订单总金额", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("order_status", 2000), createCell("TINYINT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("订单状态", 1500)
                    ]}),
                ]
            }),

            new Paragraph({ spacing: { line: 300 }, children: [] }),
            new Paragraph({
                spacing: { before: 200, after: 100 },
                children: [new TextRun({ text: "表3-4  配送任务表（t_delivery_task）", font: "黑体", size: 24, bold: true })]
            }),
            new Table({
                width: { size: 8500, type: WidthType.DXA },
                columnWidths: [2000, 1500, 2000, 1500, 1500],
                rows: [
                    new TableRow({ children: [
                        createCell("字段名", 2000, true, "D9D9D9"),
                        createCell("数据类型", 1500, true, "D9D9D9"),
                        createCell("约束", 2000, true, "D9D9D9"),
                        createCell("允许空", 1500, true, "D9D9D9"),
                        createCell("说明", 1500, true, "D9D9D9")
                    ]}),
                    new TableRow({ children: [
                        createCell("id", 2000), createCell("BIGINT", 1500),
                        createCell("PRIMARY KEY", 2000), createCell("否", 1500), createCell("主键ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("delivery_no", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("UNIQUE", 2000), createCell("否", 1500), createCell("配送单号", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("order_id", 2000), createCell("BIGINT", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("订单ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("delivery_person_id", 2000), createCell("BIGINT", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("配送人员ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("delivery_address", 2000), createCell("VARCHAR(255)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("配送地址", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("delivery_status", 2000), createCell("TINYINT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("配送状态", 1500)
                    ]}),
                ]
            }),

            new Paragraph({ spacing: { line: 300 }, children: [] }),
            new Paragraph({
                spacing: { before: 200, after: 100 },
                children: [new TextRun({ text: "表3-5  配送轨迹表（t_delivery_track）", font: "黑体", size: 24, bold: true })]
            }),
            new Table({
                width: { size: 8500, type: WidthType.DXA },
                columnWidths: [2000, 1500, 2000, 1500, 1500],
                rows: [
                    new TableRow({ children: [
                        createCell("字段名", 2000, true, "D9D9D9"),
                        createCell("数据类型", 1500, true, "D9D9D9"),
                        createCell("约束", 2000, true, "D9D9D9"),
                        createCell("允许空", 1500, true, "D9D9D9"),
                        createCell("说明", 1500, true, "D9D9D9")
                    ]}),
                    new TableRow({ children: [
                        createCell("id", 2000), createCell("BIGINT", 1500),
                        createCell("PRIMARY KEY", 2000), createCell("否", 1500), createCell("主键ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("delivery_task_id", 2000), createCell("BIGINT", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("配送任务ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("longitude", 2000), createCell("DECIMAL(10,6)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("经度", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("latitude", 2000), createCell("DECIMAL(10,6)", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("纬度", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("address", 2000), createCell("VARCHAR(255)", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("地址", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("record_time", 2000), createCell("DATETIME", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("记录时间", 1500)
                    ]}),
                ]
            }),

            new Paragraph({ spacing: { line: 300 }, children: [] }),
            new Paragraph({
                spacing: { before: 200, after: 100 },
                children: [new TextRun({ text: "表3-6  库存表（t_inventory）", font: "黑体", size: 24, bold: true })]
            }),
            new Table({
                width: { size: 8500, type: WidthType.DXA },
                columnWidths: [2000, 1500, 2000, 1500, 1500],
                rows: [
                    new TableRow({ children: [
                        createCell("字段名", 2000, true, "D9D9D9"),
                        createCell("数据类型", 1500, true, "D9D9D9"),
                        createCell("约束", 2000, true, "D9D9D9"),
                        createCell("允许空", 1500, true, "D9D9D9"),
                        createCell("说明", 1500, true, "D9D9D9")
                    ]}),
                    new TableRow({ children: [
                        createCell("id", 2000), createCell("BIGINT", 1500),
                        createCell("PRIMARY KEY", 2000), createCell("否", 1500), createCell("主键ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("dish_id", 2000), createCell("BIGINT", 1500),
                        createCell("UNIQUE", 2000), createCell("否", 1500), createCell("生鲜商品ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("current_stock", 2000), createCell("INT", 1500),
                        createCell("-", 2000), createCell("否", 1500), createCell("当前库存", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("warning_threshold", 2000), createCell("INT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("预警阈值", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("last_in_time", 2000), createCell("DATETIME", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("最后入库时间", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("last_out_time", 2000), createCell("DATETIME", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("最后出库时间", 1500)
                    ]}),
                ]
            }),

            new Paragraph({ spacing: { line: 300 }, children: [] }),
            new Paragraph({
                spacing: { before: 200, after: 100 },
                children: [new TextRun({ text: "表3-7  调度任务表（t_schedule_task）", font: "黑体", size: 24, bold: true })]
            }),
            new Table({
                width: { size: 8500, type: WidthType.DXA },
                columnWidths: [2000, 1500, 2000, 1500, 1500],
                rows: [
                    new TableRow({ children: [
                        createCell("字段名", 2000, true, "D9D9D9"),
                        createCell("数据类型", 1500, true, "D9D9D9"),
                        createCell("约束", 2000, true, "D9D9D9"),
                        createCell("允许空", 1500, true, "D9D9D9"),
                        createCell("说明", 1500, true, "D9D9D9")
                    ]}),
                    new TableRow({ children: [
                        createCell("id", 2000), createCell("BIGINT", 1500),
                        createCell("PRIMARY KEY", 2000), createCell("否", 1500), createCell("主键ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("task_no", 2000), createCell("VARCHAR(50)", 1500),
                        createCell("UNIQUE", 2000), createCell("否", 1500), createCell("任务编号", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("order_id", 2000), createCell("BIGINT", 1500),
                        createCell("UNIQUE", 2000), createCell("否", 1500), createCell("订单ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("assignee_id", 2000), createCell("BIGINT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("指派人ID", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("task_status", 2000), createCell("TINYINT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("任务状态", 1500)
                    ]}),
                    new TableRow({ children: [
                        createCell("priority", 2000), createCell("TINYINT", 1500),
                        createCell("-", 2000), createCell("是", 1500), createCell("优先级", 1500)
                    ]}),
                ]
            }),

            createTitle("3.3  功能模块设计", 2),
            createParagraph("系统功能模块主要包括：", { firstLine: 480 }),
            createParagraph("（1）生鲜商品管理模块：支持生鲜商品的增删改查、分类管理、上下架管理。", { firstLine: 480 }),
            createParagraph("（2）订单管理模块：支持订单创建、订单查询、订单取消、订单明细查看。", { firstLine: 480 }),
            createParagraph("（3）库存管理模块：支持库存查询、库存预警、库存入库、库存出库、库存记录查询。", { firstLine: 480 }),
            createParagraph("（4）配送人员管理模块：支持配送人员信息维护、状态管理、任务统计。", { firstLine: 480 }),
            createParagraph("（5）配送调度模块：支持调度任务分配、任务状态跟踪、任务优先级管理。", { firstLine: 480 }),
            createParagraph("（6）配送轨迹模块：支持位置记录、轨迹展示、地址定位（集成高德地图API）。", { firstLine: 480 }),
            createParagraph("（7）系统管理模块：支持用户管理、角色管理、数据字典管理。", { firstLine: 480 }),

            // ============ 第4章 系统实现 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            createTitle("第4章  系统实现", 1),
            createTitle("4.1  开发环境与工具", 2),
            createParagraph("（1）开发工具：IntelliJ IDEA、Visual Studio Code", { firstLine: 480 }),
            createParagraph("（2）前端技术：Vue3、TypeScript、Element Plus、Vite、ECharts", { firstLine: 480 }),
            createParagraph("（3）后端技术：Spring Boot、MyBatis-Plus、MySQL、Redis", { firstLine: 480 }),
            createParagraph("（4）地图服务：高德地图JavaScript API、Web服务API", { firstLine: 480 }),
            createParagraph("（5）版本控制：Git", { firstLine: 480 }),
            createTitle("4.2  核心功能实现", 2),
            createTitle("4.2.1  订单创建流程", 3),
            createParagraph("订单创建是系统的核心功能之一。用户选择生鲜商品并确认数量后，系统自动计算订单金额。创建订单时，系统会检查库存是否充足，如库存不足则提示用户。订单创建成功后，系统自动扣减库存并生成订单记录。", { firstLine: 480 }),
            createTitle("4.2.2  配送地址地图选点", 3),
            createParagraph("系统集成了高德地图API，支持在创建订单时通过地图选择配送位置。用户可以在地图上点击选择位置或搜索地址，系统自动获取经纬度坐标并记录地址信息。这一功能大大提高了地址录入的准确性和效率。", { firstLine: 480 }),
            createTitle("4.2.3  配送调度算法", 3),
            createParagraph("配送调度模块根据订单的配送地址、配送时间、优先级等信息，自动或手动分配配送任务给配送人员。系统支持根据配送人员的当前位置和任务状态进行智能分配，提高配送效率。", { firstLine: 480 }),
            createTitle("4.2.4  配送轨迹追踪", 3),
            createParagraph("配送人员在配送过程中可以上报当前位置，系统记录轨迹点并在地图上展示配送路线。管理人员可以实时查看配送进度，当出现异常时可以及时处理。", { firstLine: 480 }),
            createTitle("4.3  系统界面展示", 2),
            createParagraph("系统前端采用Vue3和Element Plus构建，界面简洁美观，操作便捷。主要功能模块包括：", { firstLine: 480 }),
            createParagraph("（1）登录页面：支持用户名密码登录，采用JWT认证机制。", { firstLine: 480 }),
            createParagraph("（2）首页仪表盘：展示今日订单数、待调度订单、配送中任务等关键数据。", { firstLine: 480 }),
            createParagraph("（3）生鲜商品管理页面：支持生鲜商品列表展示、新增生鲜商品、编辑生鲜商品、删除生鲜商品。", { firstLine: 480 }),
            createParagraph("（4）订单管理页面：支持订单列表、订单详情、新增订单、订单状态流转。", { firstLine: 480 }),
            createParagraph("（5）库存管理页面：支持库存列表、库存预警、入库操作、出库操作、库存记录。", { firstLine: 480 }),
            createParagraph("（6）配送调度页面：支持调度任务列表、任务分配、任务状态跟踪。", { firstLine: 480 }),
            createParagraph("（7）配送轨迹页面：支持地图展示配送轨迹、位置查询、历史轨迹回放。", { firstLine: 480 }),

            // ============ 第5章 总结与展望 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            createTitle("第5章  总结与展望", 1),
            createTitle("5.1  工作总结", 2),
            createParagraph("本文设计并实现了一套基于Spring Boot和Vue的生鲜即时配送调度系统。系统实现了生鲜商品管理、订单管理、库存管理、配送调度、配送轨迹跟踪等核心功能，有效提高了生鲜零售企业的运营效率和管理水平。", { firstLine: 480 }),
            createParagraph("主要工作总结如下：", { firstLine: 480 }),
            createParagraph("（1）完成了系统的需求分析和总体设计，确定了系统的功能模块和技术架构。", { firstLine: 480 }),
            createParagraph("（2）完成了系统的数据库设计，建立了完整的数据模型。", { firstLine: 480 }),
            createParagraph("（3）采用Spring Boot和Vue技术栈完成了系统的前后端开发。", { firstLine: 480 }),
            createParagraph("（4）集成了高德地图API，实现了配送地址地图选点和配送轨迹追踪功能。", { firstLine: 480 }),
            createParagraph("（5）对系统进行了功能测试，验证了系统的正确性和稳定性。", { firstLine: 480 }),
            createTitle("5.2  未来展望", 2),
            createParagraph("本系统虽然基本满足了生鲜零售企业的调度管理需求，但还有一些方面可以进一步优化和改进：", { firstLine: 480 }),
            createParagraph("（1）配送路径优化：引入智能算法优化配送路径，减少配送时间和成本。", { firstLine: 480 }),
            createParagraph("（2）移动端应用：开发移动端APP或小程序，方便配送人员在外勤时使用。", { firstLine: 480 }),
            createParagraph("（3）数据分析：引入机器学习技术，对订单数据进行预测分析，为经营决策提供支持。", { firstLine: 480 }),
            createParagraph("（4）多门店支持：扩展系统支持多门店管理，满足连锁企业的需求。", { firstLine: 480 }),

            // ============ 参考文献 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            createTitle("参考文献", 1),
            createParagraph("[1] 陈强. Spring Boot实战[M]. 北京: 人民邮电出版社, 2016.", { firstLine: 0 }),
            createParagraph("[2] 尤雨溪. Vue.js设计与实现[M]. 北京: 人民邮电出版社, 2022.", { firstLine: 0 }),
            createParagraph("[3] 林信良. MySQL技术内幕[M]. 北京: 机械工业出版社, 2018.", { firstLine: 0 }),
            createParagraph("[4] 高德开放平台. JavaScript API文档[EB/OL]. https://lbs.amap.com/api/jsapi-v2/.", { firstLine: 0 }),
            createParagraph("[5] Element Plus. A Vue.js 3 UI Library[EB/OL]. https://element-plus.org/.", { firstLine: 0 }),

            // ============ 致谢 ============
            new Paragraph({ pageBreakBefore: true, children: [] }),
            createTitle("致谢", 1),
            createParagraph("在本次毕业设计的过程中，我得到了指导老师的悉心指导和同学们的热心帮助。在此，我要向他们表示衷心的感谢。", { firstLine: 480 }),
            createParagraph("感谢指导老师在毕业设计期间给予我的耐心指导和帮助，从选题到系统设计再到论文撰写，老师都给予了我很多宝贵的意见和建议。", { firstLine: 480 }),
            createParagraph("感谢同学们在技术交流和互相帮助中给予的支持，大家共同探讨问题、分享经验，使我的毕业设计能够顺利完成。", { firstLine: 480 }),
            createParagraph("最后，感谢学校提供的学习环境和实验条件，让我能够将所学知识应用到实际项目中，提高了自己的专业能力和综合素质。", { firstLine: 480 }),
            new Paragraph({ spacing: { line: 600 }, children: [] }),
            createParagraph("作者：                                        日期：       年     月     日", { firstLine: 0 }),
        ]
    }]
});

const timestamp = Date.now();
const outputPath = `d:/myproject/wfxyend/a-a-a/word/生鲜即时配送调度系统_毕业设计技术报告_v${timestamp}.docx`;

Packer.toBuffer(doc).then(buffer => {
    fs.writeFileSync(outputPath, buffer);
    console.log(`论文文档已生成: 生鲜即时配送调度系统_毕业设计技术报告_v${timestamp}.docx`);
});
