# --- Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build
COPY . ./
RUN --mount=type=cache,target=/root/.gradle,id=hostup-dns-client-gradle-cache \
    --mount=type=cache,target=/root/.m2,id=hostup-dns-client-maven-cache \
    ls -alR . ; ./gradlew jar

# --- Native image build stage
FROM container-registry.oracle.com/graalvm/native-image:25-muslib AS nativebuild
ARG VERSION
WORKDIR /build
# Install UPX
ARG UPX_VERSION=5.0.2
ARG UPX_ARCHIVE=upx-${UPX_VERSION}-amd64_linux.tar.xz
RUN microdnf -y install wget xz && \
    wget -q https://github.com/upx/upx/releases/download/v${UPX_VERSION}/${UPX_ARCHIVE} && \
    tar -xJf ${UPX_ARCHIVE} && \
    rm -rf ${UPX_ARCHIVE} && \
    mv upx-${UPX_VERSION}-amd64_linux/upx . && \
    rm -rf upx-${UPX_VERSION}-amd64_linux
COPY --from=build /build/build/libs/hostup-dns-client-${VERSION}.jar hostup-dns-client-${VERSION}.jar
RUN native-image -Os --static --libc=musl -jar hostup-dns-client-${VERSION}.jar -o hostup-dns-client-static
RUN ./upx --lzma --best -o hostup-dns-client hostup-dns-client-static

# --- Final stage
FROM scratch
COPY --from=nativebuild /build/hostup-dns-client /hostup-dns-client
ENTRYPOINT ["/hostup-dns-client"]
