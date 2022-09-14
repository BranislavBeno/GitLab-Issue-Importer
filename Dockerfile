FROM gradle:7.5.1-jdk18-jammy AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
# create fat jar
RUN gradle build -x test
# move the jar file
RUN cd build/libs/ && cp gitlab-issue-importer.jar /project/
# extrect layered jar file
RUN java -Djarmode=layertools -jar gitlab-issue-importer.jar extract

FROM eclipse-temurin:18-jre-jammy
# install dumb-init
RUN apt-get update && apt-get install -y dumb-init
RUN mkdir /app
# add specific non root user for running application
RUN addgroup --system javauser && adduser --system javauser
# set work directory
WORKDIR /app
# copy jar from build stage
COPY --from=build /project/dependencies/ ./
COPY --from=build /project/snapshot-dependencies/ ./
COPY --from=build /project/spring-boot-loader/ ./
COPY --from=build /project/application/ ./
# change owner for jar directory
RUN chown -R javauser:javauser /app
# switch user
USER javauser
# run application, where dumb-init occupies PID 1 and takes care of all the PID special responsibilities
ENTRYPOINT ["dumb-init", "java", "org.springframework.boot.loader.JarLauncher"]