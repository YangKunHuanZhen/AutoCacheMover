FROM amazoncorretto:21-alpine3.16
LABEL authors="huanzhen777"

ENV file_expired_minutes=1 \
    cache_root_folder=/cache_root_folder \
    cache_folders=/cache_root_folder/download1,/cache_root_folder/download2 \
    move_target_folders=/move_target_folder/download1,/move_target_folder/download2
VOLUME \
    /cache_root_folder \
    /move_target_folder



WORKDIR /app

COPY target/AutoCacheMover-1.0-SNAPSHOT.jar ./
COPY target/lib ./lib/

ENTRYPOINT ["java","-jar","AutoCacheMover-1.0-SNAPSHOT.jar"]
