# Apache Sqoop Maven 依赖平替使用指南

本项目已将 Sqoop 原生慢速的 **Apache Ivy** 依赖下载机制平替为 **Apache Maven**，同时保留了 Sqoop 原有的全部 Ant 编译、测试、打包流程。

---

## 一、改造原理

1. **解决痛点**：
   - 原生 Sqoop 1.4.7 使用 Ivy 管理依赖，默认连接国外官方仓库且为单线程同步拉取，在国内网络环境下下载极慢、频繁超时。
2. **平替架构**：
   - **[pom.xml](file:///home/peng/桌面/projects/sqoop/pom.xml)**：统一维护 Sqoop 所需依赖，内置配置阿里云公共镜像源（`aliyun-public`、`aliyun-spring`）及 Cloudera 仓库，配置多套 `maven-dependency-plugin` 任务。
   - **[build.xml](file:///home/peng/桌面/projects/sqoop/build.xml)**：将 Ant 中原有的 `ivy:retrieve` 目标平替为自动触发 Maven 下载，并将 jar 包自动归类输出到 Ant 预期的依赖目录中构建 Classpath。

### 依赖目录结构映射表

| 目录路径 | 对应配置 | 用途说明 |
| :--- | :--- | :--- |
| `build/ivy/lib/sqoop/common/` | `common` (compile+runtime) | Sqoop 核心编译依赖（Hadoop, HBase, HCatalog, Accumulo 等） |
| `build/ivy/lib/sqoop/test/` | `test` | 单元测试依赖（JUnit, Mockito, H2, ZooKeeper 等） |
| `build/ivy/lib/sqoop/redist/` | `redist` | 随 Sqoop 发布包打包分发的运行时核心库（Avro, HSQLDB, Kite 等） |
| `build/ivy/lib/sqoop/checkstyle/` | `checkstyle` | 代码规范检查工具依赖 |
| `build/ivy/lib/sqoop/releaseaudit/` | `releaseaudit` | Apache RAT 开源协议审计依赖 |

---

## 二、使用方式

改造后支持两种使用习惯：**原生 Ant 一键驱动（推荐）** 与 **手动 Maven 预先拉取**。

### 方式 1：常规 Ant 命令（完全无感，推荐）

你可以像往常一样直接执行任何 Ant 构建命令，Ant 会自动调用 Maven 下载依赖并完成编译或打包：

```bash
# 1. 编译核心源码（会自动触发 Maven 下载依赖并编译）
ant compile

# 2. 生成核心 Sqoop Jar 包（输出在 build/sqoop-1.4.7.jar）
ant jar

# 3. 编译测试用例
ant compile-test

# 4. 执行测试
ant test

# 5. 生成完整发行版 tar.gz 安装包
ant package
```

> **说明**：只要执行过一次，Maven 会自动将依赖下载到本地 `~/.m2/repository` 缓存中，并输出至 `build/ivy/lib/sqoop/`。后续再次执行 `ant` 命令时，无需重复从网络下载。

---

### 方式 2：手动使用 Maven 预拉取依赖

如果你想在断网离线编译前，或者手动确认依赖全部下载完成，可直接在项目根目录下执行：

```bash
# 一键并行下载所有依赖并自动分发到 build/ivy/lib/sqoop/* 对应目录
mvn generate-resources -DskipTests
```

执行完毕后，检查依赖是否准备就绪：
```bash
ls -lh build/ivy/lib/sqoop/common/
ls -lh build/ivy/lib/sqoop/test/
ls -lh build/ivy/lib/sqoop/redist/
```
确认无误后，即可随时执行 `ant compile` 或 `ant package`。

---

## 三、构建清理与重新下载

- **常规清理（仅清理编译产物，保留已下载依赖）**：
  ```bash
  ant clean
  ```
- **深度清理（同时清理已下载依赖，强制重新拉取）**：
  ```bash
  ant clean
  rm -rf build/ivy
  ```

---

## 四、常见问题说明

1. **本地 settings.xml 是否需要修改？**
   - **不需要**。所有加速镜像源（阿里云公共仓库、阿里云 Spring 插件库、Cloudera 仓库）均已在项目的 [pom.xml](file:///home/peng/桌面/projects/sqoop/pom.xml) 中独立声明，不侵入和依赖用户系统的全局配置。
2. **Java 与开发测试环境要求**：
   - JDK 1.8（已配置）
   - Apache Maven 3.x（已配置）
   - Apache Ant 1.9+（已配置）
