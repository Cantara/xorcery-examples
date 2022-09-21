# Amazon Corretto OpenJDK 18 image (https://hub.docker.com/_/amazoncorretto)
FROM amazoncorretto:19.0.0
# copy application jar (with libraries inside)
ARG JAR_VERSION
COPY target/xorcery-examples-${JAR_VERSION}-launcher.jar /xorcery-examples.jar
# specify default command
ENV RESOURCES="jar:file:/xorcery-examples.jar!"
ENV HOME="/var/lib/xorcery-examples"
ENV JAVA_OPTS="-Xms512m -Xmx1g"
ENTRYPOINT exec /usr/lib/jvm/java-18-amazon-corretto/bin/java $JAVA_OPTS \
-Dlog4j2.configurationFile=log4j2.yaml \
--add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED \
--add-opens=java.base/java.nio=ALL-UNNAMED \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
-jar /xorcery-examples.jar
