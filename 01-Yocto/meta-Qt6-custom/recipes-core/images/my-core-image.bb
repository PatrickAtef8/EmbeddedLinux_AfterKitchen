SUMMARY = "A simple image with a GUI using Qt"
LICENSE = "CLOSED"

inherit core-image

IMAGE_FEATURES += "splash ssh-server-dropbear weston package-management"

IMAGE_INSTALL:append = " \
    qtwayland connman-client bash \
    systemd systemd-analyze systemd-boot \
    weston-init \
    myqt \
"


# image install lw 3ayz 7aga generic tnzl m3aya fel image msh mortbta bel recipe bt3ty 
#lakn lw 3ayz 7aga mo3tmd 3leha bs el recipe bt3ty b3ml depends msh image install

IMAGE_ROOTFS_EXTRA_SPACE = "5242880"
