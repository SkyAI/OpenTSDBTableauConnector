## OpenTSDB Web Data Connector for Tableau

Introduce
---------

OpenTSDB Web Data Connector for Tableau in java.


Develop
-------

__properties__

- application.properties
   - 该配置为应用配置


Get Start
---------

__run application__

 1. `yum install maven git`
 2. `git clone https://github.com/SkyAI/OpenTSDBTableauConnector`
 3. `vim OpenTSDBTableauConnector/src/main/resources/application.properties`，完成应用配置
 4. 在根目录下，使用命令：`mvn install`
 5. `cd target`
 6. 运行jar包 `java -jar ****.jar`

__open front page__

 1. 打开Tableau 选择 Web Data Connector
 2. 输入网址http://`{host}`:`{port}`，port默认是8080
 3. 输入OpenTSDB的host和port
 4. 选择数据范围
 5. 点击`Get Data`
