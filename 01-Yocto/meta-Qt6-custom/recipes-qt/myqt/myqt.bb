SUMMARY = "This Recipe is for compiling Qt app"
LICENSE = "CLOSED"

SRC_URI = "git://github.com/PatrickAtef8/Qt6-App-Yocto.git;branch=main;protocol=https"
SRCREV = "96aad6630c62a645430b72e2baa16fdf5a449d28"
S = "${WORKDIR}/git"

inherit qt6-cmake systemd

DEPENDS += " \
    qtbase \
    qtdeclarative-native \
    qtmultimedia \
    qtwayland \
"

RDEPENDS:${PN} += " \
    qtbase \
    qtdeclarative \
    qtmultimedia \
    qtwayland \
"

EXTRA_OECMAKE += "-DQT_HOST_PATH=${STAGING_DIR_NATIVE}/usr"
EXTRA_OECMAKE += "-DQT_HOST_PATH_CMAKE_DIR=${STAGING_DIR_NATIVE}/usr/lib/cmake"
EXTRA_OECMAKE += "-DCMAKE_PREFIX_PATH=${STAGING_DIR_TARGET}/usr/lib/cmake"
EXTRA_OECMAKE += "-DCMAKE_FIND_ROOT_PATH=${STAGING_DIR_TARGET}"
EXTRA_OECMAKE += "-DCMAKE_SYSTEM_NAME=Linux"


do_install() {
    install -d ${D}${bindir}
    
    if [ ! -f "${B}/qt-gpio-app" ]; then
        echo "Error: qt-gpio-app not found in ${B}!" >&2
        exit 1
    fi
    
    install -m 0755 ${B}/qt-gpio-app ${D}${bindir}/qt-gpio-app
}

FILES:${PN} += "${bindir}/qt-gpio-app"
