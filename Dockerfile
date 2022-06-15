FROM openjdk:11-jdk
ARG HONKBOT_TOKEN
ENV HONKBOT_TOKEN=${HONKBOT_TOKEN}
ENV PORT=6969
ENV HONKBOT_LOG="INFO"
ENV RULEBOT_HOST="0.0.0.0"
EXPOSE 6969:6969
EXPOSE 6970:6970
RUN mkdir /app
COPY ./bazel-bin/myrulebot/app_deploy.jar /app
WORKDIR /app
CMD ["java","-jar","./app_deploy.jar"]