# common-storage — 对象存储公共模块

> 路径: `ai-common/common-storage/`

## 定位

统一对象存储（OSS）服务，支持文件上传、预签名 URL 生成、文件删除。

## 核心类

| 类 | 职责 |
|---|---|
| `config/OssConfig.java` | @ConfigurationProperties(prefix="oss")，读取 endpoint/accessKeyId/accessKeySecret/bucketName |
| `service/OssStorageService.java` | Aliyun OSS 存储服务 |

## 关键设计

- **upload(MultipartFile, directory, appId)**：上传文件到 OSS，路径格式 `{appId}/{directory}/{uuid}_{filename}`
- **generatePresignedUrl(objectName, expireMinutes)**：生成临时访问 URL
- **delete(objectName)**：删除文件
- appId 作为路径段，天然实现多租户存储隔离

## 依赖

- common-core
- spring-boot-starter-web（MultipartFile）
- Aliyun OSS SDK
