FROM alpine:latest
ARG TARFILE=temporal_cli_latest_linux_amd64.tar.gz
RUN apk add --no-cache wget tar
RUN wget 'https://temporal.download/cli/archive/latest?platform=linux&arch=amd64' -O $TARFILE
RUN tar xf $TARFILE
RUN rm $TARFILE
RUN mv temporal /usr/local/bin
ENTRYPOINT ["temporal"]
CMD ["server", "start-dev", "--ui-ip=0.0.0.0", "--log-config"]