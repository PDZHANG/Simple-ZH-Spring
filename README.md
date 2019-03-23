# Simpler-ZH-Spring
从0开始编写spring 框架，简易实现spring，理解核心原理。 参考咕泡学院tom老师的视频编写而成

## 常见注解实现
@Controller  @Service @Autowired 等

## 1.加载配置文件

## 2.根据配置文件进行处理，比如扫描包和类

## 3.利用反射机制，把每一个类进行实例化，存储到自定义的IOC容器中

## 4.利用反射和自定义的IOC容器，把配置有@Autowired 注解的字段，进行依赖注入

## 5.初始化 controller 和 url之间的映射关系（利用注解@Controller 和 @ReqestMapping ）

## 6.处理请求分发，根据映射关系把对应的url处理到对应的类
