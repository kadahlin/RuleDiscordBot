FROM openjdk:8-jdk
ENV PORT=6969
EXPOSE 6969:6969
RUN mkdir /app
COPY ./build/install/myrulebot/ /app/
WORKDIR /app/bin
CMD ["./myrulebot"]