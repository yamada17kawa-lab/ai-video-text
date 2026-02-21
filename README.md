# AI Video 项目

基于 Spring Boot + LangChain4j + 阿里云服务构建的 AI 视频语音识别与知识库问答系统。

## 项目架构

```
ai-video/
├── ai-asr         # 语音识别服务（阿里云 ASR + OSS）
├── ai-gateway     # API 网关（Spring Cloud Gateway）
├── ai-langchain4j # RAG 知识库服务（LangChain4j + pgvector）
└── common         # 公共模块
```

## 技术栈

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.x**
- **Spring Cloud Alibaba (Nacos)**
- **LangChain4j 1.0.0-beta3**
- **PostgreSQL + pgvector**
- **MyBatis-Plus 3.5.7**
- **阿里云 OSS**
- **阿里云 ASR（语音识别）**
- **通义千问（text-embedding-v2）**

## 快速开始

### 前置要求

- JDK 21+
- Maven 3.8+
- PostgreSQL 15+（需安装 pgvector 扩展）
- Nacos 2.3+
- Redis（可选，用于缓存）

### 数据库初始化

创建数据库并执行以下 SQL：

```sql
-- 创建数据库
CREATE DATABASE aivideo;

-- 连接到 aivideo 数据库
\c aivideo;

-- 创建向量数据表（存储文档嵌入向量）
CREATE TABLE vector_data (
    embedding_id VARCHAR(64) PRIMARY KEY,
    embedding VECTOR(1536),  -- 通义千问 text-embedding-v2 的维度为 1536
    metadata JSONB,
    text TEXT NOT NULL
);

-- 创建资源表（存储文件资源信息）
CREATE TABLE resources (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url TEXT NOT NULL
);

-- 创建索引（可选，用于加速向量搜索）
CREATE INDEX ON vector_data USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```

### 配置说明

各服务的配置通过 Nacos 配置中心管理，请确保 Nacos 中存在以下配置：

- `ai-video.yml` - 全局配置
- `ai-asr.yml` - ASR 服务配置
- `ai-langchain4j.yml` - RAG 服务配置

本地开发可使用 `application-local.yml` 覆盖配置。

### 构建与运行

```bash
# 编译整个项目
mvn clean install -DskipTests

# 启动顺序
# 1. 启动 Nacos
# 2. 启动 ai-gateway（端口 8080）
# 3. 启动 ai-asr（端口 8432）
# 4. 启动 ai-langchain4j（端口 9432）
```

## 服务说明

### ai-asr（语音识别服务）

- **端口**: 8432
- **功能**:
  - 阿里云 OSS 文件上传/下载
  - 视频转音频
  - 阿里云 ASR 语音识别
  - 转写文本存储

### ai-langchain4j（知识库服务）

- **端口**: 9432
- **功能**:
  - 文档向量化（通义千问嵌入模型）
  - 向量存储（pgvector）
  - RAG 问答
  - 文档管理

### ai-gateway（API 网关）

- **端口**: 8080
- **功能**:
  - 路由转发
  - 负载均衡
  - 服务发现集成

## 主要 API

### ASR 服务

- `POST /api/asr/upload` - 上传文件到 OSS
- `POST /api/asr/transcribe` - 语音识别转写

### 知识库服务

- `POST /api/documents` - 上传文档
- `POST /api/embeddings` - 向量化文档
- `POST /api/ai/chat` - RAG 问答

## 许可证

MIT License
