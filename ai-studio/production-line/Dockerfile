# 该镜像需要依赖的基础镜像
FROM reg.yunchensys.com:20180/naic-online/openjdk:11
# 将targer目录下的jar包复制到docker容器/home/springboot目录下面目录下面
ADD ai-studio-production-line.jar /home/springboot/ai-studio-production-line.jar
# 执行命令
CMD ["java","-jar","-Xmx400m","-Duser.timezone=GMT+08","-Djdk.tls.client.protocols=TLSv1.2","/home/springboot/ai-studio-production-line.jar"]