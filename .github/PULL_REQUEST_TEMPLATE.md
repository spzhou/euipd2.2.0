## 变更类型

- [ ] 缺陷修复
- [ ] 新功能
- [ ] 不兼容变更（影响现有功能、接口或数据结构）
- [ ] 文档
- [ ] 重构或性能改进

## 变更说明

说明本次变更的内容与原因。

## 关联 Issue

Closes #

## 验证

- 运行环境（JDK、Node.js、操作系统）：
- 验证步骤与结果：

## 检查清单

- [ ] 后端编译通过，前端 `npm run lint` 无报错
- [ ] 未提交口令、密钥、令牌或内部地址
- [ ] 表结构变更已更新 `euipdv2-master/doc/db/euipd-schema.sql`，并在说明中附升级用的 ALTER 语句
- [ ] 新增配置项已登记到 `docker/env.example` 与根目录 `README.md`
- [ ] 影响使用方式的变更已更新文档与 `CHANGELOG.md`
