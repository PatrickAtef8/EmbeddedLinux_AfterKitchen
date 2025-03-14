SRC_URI:append = " file://myqt.service"

SYSTEMD_AUTO_ENABLE = "enable" 
SYSTEMD_SERVICE:${PN} = "myqt.service"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/myqt.service ${D}${systemd_unitdir}/system/myqt.service
}
