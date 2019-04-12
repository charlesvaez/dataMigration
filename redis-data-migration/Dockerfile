#基于哪个镜像
FROM openjdk:8
#指定维护者信息
MAINTAINER hcx
#指定版本信息
LABEL app="redis-data-migration" version="0.0.1" by="hcx"
#指定挂载点
VOLUME /logs
#复制文件到容器
ADD ./redis-data-migration.jar app.jar
#RUN sh -c 'touch /app.jar'
#ENV JAVA_OPTS=""
EXPOSE 8088
#配置容器启动后执行的命令
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
