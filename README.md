# FTP Traccar Client for Android Based on [Traccar](https://www.traccar.org/client)

## Overview

FTP Traccar Client is an Android GPS tracking application. It can work with Traccar open source server software.


## Build
[docker-android-build-box](https://hub.docker.com/r/mingc/android-build-box/)
`docker run --rm -v "${HOME}/.dockercache/gradle":"/root/.gradle" -v ${PWD}:/project mingc/android-build-box bash -c 'cd /project; ./gradlew build'`

## Test
`docker run --rm -v "${HOME}/.dockercache/gradle":"/root/.gradle" -v ${PWD}:/project mingc/android-build-box bash -c 'cd /project; ./gradlew test -i'`

## Notes
APK deploying has only been tested through Docker on Linux [Docker sorccu/adb](https://hub.docker.com/r/sorccu/adb/)

### Deploy over ADB in Docker on Linux
`docker run -it --rm --privileged --net=host -v ${PWD}:/project -v /dev/bus/usb:/dev/bus/usb sorccu/adb sh -c "adb start-server && adb install -r /project/app/build/outputs/apk/regular/debug/app-regular-debug.apk"`
