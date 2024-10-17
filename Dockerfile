FROM azul/zulu-openjdk-alpine:21.0.5 AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
# create fat jar
RUN chmod +x gradlew && ./gradlew app:assemble && cp app/build/libs/gitlab-issue-importer.jar ./
# extrect layered jar file
RUN java -Djarmode=layertools -jar gitlab-issue-importer.jar extract

FROM azul/zulu-openjdk-alpine:21.0.5-jre
# install dumb-init
RUN apk add --no-cache dumb-init=1.2.5-r3
RUN mkdir /gii
# add specific non root user for running application
RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
# set work directory
WORKDIR /gii
# copy jar from build stage
COPY --from=build /project/spring-boot-loader/ ./
COPY --from=build /project/snapshot-dependencies/ ./
COPY --from=build /project/dependencies/ ./
COPY --from=build /project/application/ ./
# change owner for jar directory
RUN chown -R javauser:javauser /gii
# switch user
USER javauser
# run application, where dumb-init occupies PID 1 and takes care of all the PID special responsibilities
ENTRYPOINT ["dumb-init", "java", "org.springframework.boot.loader.launch.JarLauncher"]
