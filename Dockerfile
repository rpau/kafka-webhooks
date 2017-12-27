FROM maven:3.5.2-jdk-8-alpine
RUN mkdir app
ADD . /app
WORKDIR app
RUN mvn clean package
RUN chmod u+x webhooks_standalone.sh
EXPOSE 8081
CMD ["./webhooks_standalone.sh"]
