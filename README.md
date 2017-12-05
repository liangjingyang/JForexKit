# JForex Kit

包含了JForex API的Android Studio项目, 可以直接用Android Studio打开. 

### 文档

JForex API开发文档:

[英文官方原版](https://www.dukascopy.com/wiki/en/development)

[中文版持续更新中](https://www.jforexcn.com/development)

### 编译

`./gradlew uberJar`

编译Jar包, 在build/libs/目录下生成JForexCN-3.0.jar文件.

### 使用

本项目是以Standalone的方式(命令行下, 无GUI)运行.

下面命令可以显示用法:

`java -jar JForexCN-3.0.jar`

命令支持两个参数, `command`和`strategy`.

`java -jar JForexCN-3.0.jar [command] [strategy]`

其他参数在配置文件中, 配置文件请放在`JForexCN-3.0.jar`同目录下, 命名为`JForexCN.properties`, 可以通过拷贝项目里的`JForexCN.properties.template`来生成配置文件:

`cp JForexCN.properties.template JForexCN.properties`

### 用例

以`OrderWatcher`策略为例:

1. 对策略进行历史测试

`java -jar JForexCN-3.0.jar test OrderWatcher`

2. 在模拟盘运行策略:

`java -jar JForexCN-3.0.jar demo OrderWatcher`

3. 在实盘运行策略:

`java -jar JForexCN-3.0.jar live OrderWatcher`


4. 发送测试邮件, 测试邮件配置:

`java -jar JForexCN-3.0.jar email`

### 其他

1. 项目里的`com.jforexcn.wiki`下面是[JForex中文Wiki](https://www.jforexcn.com/development)里面设计到的策略, 指标, 和插件. 会随着文档的跟新陆续补充.
2. 在运行命令的时候, 只有`com.jforexcn.shared.strategy`里的策略才会被自动识别到, 如果像测试`com.jforexcn.wiki.strategy`里的策略, 可以手动将其拷贝或移动到`com.jforexcn.shared.strategy`下面.