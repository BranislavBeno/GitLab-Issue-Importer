FROM azul/zulu-openjdk-alpine:25.0.4.1 AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
# create fat jar
RUN chmod +x gradlew && ./gradlew app:assemble && cp app/build/libs/gitlab-issue-importer.jar ./
# extrect layered jar file
RUN java -Djarmode=tools -jar gitlab-issue-importer.jar extract --layers --launcher --destination extracted

FROM azul/zulu-openjdk-alpine:25.0.4.1-jre-headless
# install dumb-init
RUN apk update
RUN apk add --no-cache --upgrade dumb-init
RUN mkdir /gii
# add specific non root user for running application
RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
# set work directory
WORKDIR /gii
# copy jar from build stage
COPY --from=build /project/extracted/spring-boot-loader/ ./
COPY --from=build /project/extracted/snapshot-dependencies/ ./
COPY --from=build /project/extracted/dependencies/ ./
COPY --from=build /project/extracted/application/ ./
# change owner for jar directory
RUN chown -R javauser:javauser /gii
# switch user
USER javauser
# run application, where dumb-init occupies PID 1 and takes care of all the PID special responsibilities
ENTRYPOINT ["dumb-init", "java", "org.springframework.boot.loader.launch.JarLauncher"]
