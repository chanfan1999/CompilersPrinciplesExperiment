## 采用JavaFX+Kotlin完成编译原理作业模板

采用Maven进行项目管理，适用于JavaFX构建的基本框架。

通过maven中的jlink命令生成最小JRE环境，通过maven中的package命令生成包含依赖的jar包，以便后续打包处理。

jar包在target目录生成，包含依赖的jar包名字为

```
$ProjectName-1.0-SNAPSHOT-jar-with-dependencies.jar
```

.gitignore文件包含传统Maven、kotlin和IntelliJ的生成文件过滤。[由此](https://www.toptal.com/developers/gitignore)生成。
