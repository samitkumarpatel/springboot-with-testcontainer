FROM alpine:latest
ARG TARFILE=temporal_cli_latest_linux_amd64.tar.gz
RUN apk add --no-cache wget tar
RUN wget 'https://temporal.download/cli/archive/latest?platform=linux&arch=amd64' -O $TARFILE
RUN tar xf $TARFILE
RUN rm $TARFILE
RUN mv temporal /usr/local/bin
EXPOSE 7233 8233 43971
CMD ["temporal", "server", "start-dev", "--ui-ip=0.0.0.0", "--ip=0.0.0.0", "--log-config"]