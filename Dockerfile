FROM openjdk:8-jdk
ARG HONKBOT_TOKEN
ENV HONKBOT_TOKEN=${HONKBOT_TOKEN}
ENV PORT=6969
ENV HONKBOT_LOG="INFO"
ENV RULEBOT_HOST="0.0.0.0"
EXPOSE 6969:6969
RUN mkdir /app
COPY ./myrulebot/build/install/myrulebot/ /app/
WORKDIR /app/bin
CMD ["./myrulebot"]