<h1 align="center">Open Platform</h1>

<p align="center">
<img src="https://img.shields.io/github/stars/brotherc/openplatform" alt="GitHub Stars">
<img src="https://img.shields.io/github/license/brotherc/openplatform" alt="LICENSE">
<img src="https://img.shields.io/badge/jdk-%3E%3D17-green" alt="JDK support">
</p>

# 🎯 介绍
`Open Platform`是企业对内对外统一开放接口能力的核心平台，提供标准化的API管理等功能。通过结构化的文档管理与灵活的接口接入机制，开放平台旨在为内部系统集成和第三方开发者接入提供便捷的能力出口。

<br>

# ✨ 核心功能
## 门户端
- 首页展示
- 文章展示
- API展示

## 管理端
### 文档中心
- 提供文档分组管理
- 支持文档目录树创建、文章编写，支持文章管关联API来展示信息

### API中心
- 支持API分类树、API创建

<br>

# 🧱 技术栈
## 🔧 后端技术
| 模块    | 技术方案                     |
|-------|--------------------------|
| 基础框架  | Spring Boot 3.x, WebFlux |
| 数据访问  | R2DBC + MySQL            |
| 持久化工具 | Spring Data R2DBC        |
| 搜索引擎  | Elasticsearch            |
| 构建工具  | Maven                    |
| 前端    | Vue3                     |

## 💻 前端技术
| 模块       | 技术                             |
|------------|----------------------------------|
| 框架       | Vue 3 (Composition API)          |
| 构建工具   | Vite 5                            |
| 路由       | Vue Router 4                     |
| 组件库     | Ant Design Vue 4                 |
| 表格组件   | VXE-Table                         |
| 网络请求   | Axios                             |

<br>

# 📦 快速启动
## ✅ 环境准备

- JDK 17+
- MySQL 8.x+
- Elasticsearch 8.x+
- Maven 3.6+
- Node.js >=16.0.0
- npm >= 8.x

## 🏁 启动方式
openplatform-backend：以springboot方式启动`com.brotherc.documentcenter.OpenPlatformApplication`  
openplatform-frontend：以npm方式启动`npm run dev`

<br>

# 👁 界面预览
![分组管理](https://github.com/Brotherc/openplatform/blob/main/doc/manager/doccataloggroup.png)  

<br>

# ⚙ 后续规划
- 开发者管理
- 应用管理
- API调用接入

