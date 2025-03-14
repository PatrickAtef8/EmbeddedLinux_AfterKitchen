# Yocto Setup and Configuration Guide for Custom Image for a qt application

## Table of Contents

- **Introduction to Yocto Project**  
- **Setting Up the Yocto Build System (Step-by-Step Guide)**  
- **Creating and Configuring a Custom Meta Layer for a Qt Application**  
- **Key Yocto Terminologies and Variable Explanations**  
- **Overview of Custom Layer Files and Their Responsibilities (ElZatona)**  🫒

## Introduction to Yocto

### What is Yocto?
Yocto is an open-source project that provides a flexible and customizable framework for building embedded Linux distributions. It is used to create lightweight, optimized, and production-ready Linux images for embedded systems.

### Yocto vs. Buildroot
| Feature        | Yocto | Buildroot |
|---------------|-------|-----------|
| Customization | High  | Medium    |
| Complexity    | High  | Low       |
| Package Management | Yes (BitBake recipes) | No (Monolithic build) |
| Dependency Management | Yes | No |
| Reproducibility | High | Medium |
| Target Audience | Advanced embedded developers | Quick prototyping and simple devices |

### What is Poky?
Poky is the reference distribution of Yocto. It includes BitBake (Yocto's build system), core metadata, and a set of example configurations to help developers start building Linux images.

### What is OpenEmbedded?
OpenEmbedded (OE) is the foundation of Yocto. It provides core metadata, package recipes, and BitBake as the build system. OpenEmbedded was initially a standalone project but merged with Yocto to enhance development efficiency.

### What is a Layer and a Meta Layer?
A **layer** is a collection of metadata, recipes, and configurations used to build a Linux system. A **meta-layer** is a specific type of layer that provides reusable software components (e.g., `meta-qt6` for Qt6 support, `meta-raspberrypi` for Raspberry Pi support).

### Yocto Releases (Kirkstone, Dunfell, etc.)
Yocto has versioned releases named after stones. Examples include:
- **Dunfell (3.1)** - LTS version
- **Kirkstone (4.0)** - LTS version, modern and widely used
- **Langdale (4.1)**, **Mickledore (4.2)** - Newer releases

Yocto solves incompatibility issues between versions by enforcing stable APIs, backward compatibility layers, and well-documented migration guides.

### What is a Recipe?
A recipe (`.bb` file) in Yocto defines how to fetch, configure, compile, and install a package.

### What is BitBake?
BitBake is the build engine of Yocto. It parses recipes, resolves dependencies, and executes build tasks.

---

## Setting Up Yocto for Raspberry Pi 3B+

### 1. Create a Yocto Directory
```sh
mkdir yocto
cd yocto
```

### 2. Clone Required Meta Layers
```sh
git clone git@github.com:yoctoproject/poky.git
cd poky && git checkout kirkstone && cd ..

git clone git://git.openembedded.org/meta-openembedded
cd meta-openembedded && git checkout kirkstone && cd ..

git clone git@github.com:agherzan/meta-raspberrypi.git
cd meta-raspberrypi && git checkout kirkstone && cd ..

git clone git@github.com:shr-project/meta-qt6.git
cd meta-qt6 && git checkout jansa/kirkstone && cd ..
```

### 3. Create a Build Directory and Source the Environment
```sh
mkdir buildrpi3b
source poky/oe-init-build-env buildrpi3b
```
The `oe-init-build-env` script sets up the necessary environment variables and prepares the build environment.

### 4. Add Required Layers to `bblayers.conf`
```sh
bitbake-layers add-layer ../meta-openembedded/meta-oe
bitbake-layers add-layer ../meta-openembedded/meta-python
bitbake-layers add-layer ../meta-qt6
bitbake-layers add-layer ../meta-raspberrypi
```

### 5. Create a Custom Layer
```sh
bitbake-layers create-layer ../meta-Qt6-custom
bitbake-layers add-layer ../meta-Qt6-custom
```

### 6. Understanding `bblayers.conf`
The `bblayers.conf` file defines which meta-layers are included in the Yocto build process. It is located in `conf/` inside the build directory. It ensures that all required layers are available for the build.

Example `bblayers.conf`:
```conf
BBLAYERS ?= " \
  /home/patrick/yocto/poky/meta \
  /home/patrick/yocto/poky/meta-poky \
  /home/patrick/yocto/poky/meta-yocto-bsp \
  /home/patrick/yocto/meta-openembedded/meta-oe \
  /home/patrick/yocto/meta-openembedded/meta-python \
  /home/patrick/yocto/meta-qt6 \
  /home/patrick/yocto/meta-Qt6-custom \
  /home/patrick/yocto/meta-raspberrypi \
  "
```

### 7. Understanding `local.conf`
The `local.conf` file provides global configuration settings for the Yocto build system. It includes machine selection, build paths, and other important settings.

#### Key Variables in `local.conf`:
- **MACHINE = "raspberrypi3-64"**
  - Specifies the target hardware. In this case, it is a 64-bit Raspberry Pi 3.
- **ENABLE_UART = "1"**
  - Enables the UART interface on the Raspberry Pi.
- **SSTATE_DIR ?= "${TOPDIR}/sstate-cache"**
  - Defines the shared state directory where Yocto caches build artifacts to speed up future builds.
- **DL_DIR ?= "${TOPDIR}/downloads"**
  - Specifies the directory for storing downloaded source files.
- **DISTRO ?= "mydistro"**
  - Defines the distribution name. This can be customized to create a unique embedded Linux distribution.

### 8. Configure `local.conf`
Edit `conf/local.conf` and modify:
```conf
MACHINE = "raspberrypi3-64"
ENABLE_UART = "1"
SSTATE_DIR ?= "${TOPDIR}/sstate-cache"
DL_DIR ?= "${TOPDIR}/downloads"
DISTRO ?= "mydistro"
```

Now your Yocto environment is ready for development!



# Yocto Meta-Qt6 Custom Layer Guide

## Introduction
This guide provides an in-depth explanation of setting up a custom Yocto layer (`meta-Qt6-custom`) to build an embedded Linux image for Raspberry Pi 3B+ with Qt6 and systemd support. We will cover layer structure, configuration files, recipes, and detailed explanations of all variables used.

---

## Custom Layer Structure
Ensure your layer has the following directory structure:

```
meta-Qt6-custom/
├── conf
│   ├── distro
│   │   └── mydistro.conf
│   └── layer.conf
├── COPYING.MIT
├── README
├── recipes-core
│   └── images
│       └── my-core-image.bb
├── recipes-qt
│   └── myqt
│       ├── files
│       │   └── myqt.service
│       ├── myqt.bb
│       └── myqt.bbappend
```

---

## 1. `mydistro.conf` - Custom Distribution Configuration
### Copy and Modify Poky Configuration
Copy the content of `poky.conf` from `meta-poky/conf/distro/` to `mydistro.conf` and modify the following details:

```conf
DISTRO = "mydistro"
DISTRO_NAME = "My Custom Distro"
DISTRO_VERSION = "1.0"
MAINTAINER = "Patrick Altouf <patrick@example.com>"

DISTRO_FEATURES:append = " systemd pam"
DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"
VIRTUAL-RUNTIME_init_manager = "systemd"
```

### Explanation of Variables:
- **DISTRO**: Defines the distribution name.
- **DISTRO_NAME**: A human-readable name for your custom Linux distribution.
- **DISTRO_VERSION**: Version number of the distribution.
- **MAINTAINER**: Contact details of the distribution maintainer.
- **DISTRO_FEATURES**: Adds specific features to the build (`systemd` for init system, `pam` for authentication management).
- **DISTRO_FEATURES_BACKFILL_CONSIDERED**: Prevents sysvinit from being added automatically.
- **VIRTUAL-RUNTIME_init_manager**: Specifies systemd as the init manager instead of sysvinit.

---

## 2. `layer.conf` - Layer Configuration
Modify `layer.conf` to specify dependencies:

```conf
LAYERDEPENDS_meta-Qt6-custom = "core qt6-layer openembedded-layer meta-python raspberrypi"
```

### Explanation of Variables:
- **LAYERDEPENDS_meta-Qt6-custom**: Lists dependencies for this layer.

---

## 3. `my-core-image.bb` - Image Recipe
This recipe defines the image that will be built.

```conf
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

IMAGE_ROOTFS_EXTRA_SPACE = "5242880"
```

### Explanation of Variables:
- **inherit core-image**: Inherits the `core-image` class to create a root filesystem.
- **IMAGE_FEATURES**: Adds features like a splash screen, SSH server, and Wayland compositor.
- **IMAGE_INSTALL**: Installs specified packages in the root filesystem.
- **IMAGE_ROOTFS_EXTRA_SPACE**: Reserves extra space (5MB) in the root filesystem.

---

## 4. `myqt.bb` - Qt Application Recipe
Defines how the Qt application is built and installed.

```conf
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

FILES:${PN} += "${bindir}/qt-gpio-app"
```

### Explanation of Variables:
- **SRCREV**: Specifies a fixed commit hash to ensure reproducible builds.
  Check the latest commit hash (SRCREV) ->`git ls-remote https://github.com/PatrickAtef8/Qt6-App-Yocto.git main`
- **inherit qt6-cmake systemd**: Inherits Qt6 build system and systemd support.
- **DEPENDS**: Specifies build-time dependencies.
- **RDEPENDS**: Specifies runtime dependencies.
- **SRC_URI**: Defines the repository containing the source code(your qt app files).
- **S**: Specifies the source directory within `WORKDIR`.
- **bindir**: The directory where binaries are installed (`/usr/bin`).
- **WORKDIR**: The working directory where the source is extracted and built.
- **PN**: The package name of the recipe.
- **B**: The build directory where compilation occurs.
- **D**: The destination directory where files are staged before packaging.

---

## 5. `myqt.bbappend` - Systemd Integration

```conf
SRC_URI:append = " file://myqt.service"

SYSTEMD_AUTO_ENABLE = "enable" 
SYSTEMD_SERVICE:${PN} = "myqt.service"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/myqt.service ${D}${systemd_unitdir}/system/myqt.service
}
```

### Explanation of Variables:
- **systemd_unitdir**: The directory where systemd unit files are installed (`/lib/systemd` or `/etc/systemd`).

---

## 6. `myqt.service` - Systemd Service File

```conf
[Unit]
Description=My Qt Application
After=weston.service
Requires=weston.service

[Service]
Environment="XDG_RUNTIME_DIR=/run/user/1000"
Environment="WAYLAND_DISPLAY=wayland-1"
Environment="QT_QPA_PLATFORM=wayland"
Environment="QT_PLUGIN_PATH=/usr/lib/plugins"
ExecStartPre=/bin/sleep 10
ExecStart=/usr/bin/qt-gpio-app
Restart=always
RestartSec=5
User=weston
Group=weston

[Install]
WantedBy=default.target
```

### Explanation of Variables:
- **After=weston.service**: Ensures the app starts after the Weston compositor.
- **Requires=weston.service**: Makes Weston a required dependency.
- **ExecStartPre**: Delays execution to ensure Weston is ready.
- **ExecStart**: Runs the Qt application.
- **Restart**: Ensures the service restarts on failure.
- **User & Group**: Runs as `weston`, avoiding root permissions.


# Why is `weston` Used as the User & Group in `myqt.service`?

Setting `User=weston` and `Group=weston` ensures **security and compatibility** with the Wayland compositor.

### Key Reasons:
1. **Security** – Running as `root` is risky; any vulnerability in the app could compromise the entire system.
2. **Wayland Access** – Weston runs under `weston`, and Wayland restricts display access to the same user.
3. **File Permissions** – Weston owns necessary sockets and runtime files, preventing permission errors.

### What If `User=root`?
- The application may **fail to start** due to Wayland restrictions.
- It would run with **elevated privileges**, increasing security risks.
- Any exploit could affect the entire system.
---

# Summary 🫒
This guide provides a complete understanding of setting up a custom Yocto layer with Qt6 and systemd integration. Every configuration and variable has been explained in detail. Let me know if you need further clarifications!

## Yocto Terminology FastRead Eraya kda morag3a kol 7aga bt3ml eh 🫒

### 1. **Yocto Project**
A framework for creating custom Linux distributions for embedded systems. It provides tools, metadata, and workflows for cross-compilation and package management.

### 2. **Poky**
Poky is the reference distribution of Yocto. It includes BitBake, OpenEmbedded-Core, and essential meta-layers.

### 3. **BitBake**
The build tool that processes recipes (`.bb` files) and executes tasks to build packages and images.

### 4. **OpenEmbedded (OE)**
A build system that provides a set of metadata (recipes, classes, configurations) for cross-compiling embedded Linux systems.

### 5. **Meta-Layers**
Modular collections of recipes and configurations that organize Yocto builds. Examples:
   - `meta-raspberrypi` – Provides BSP support for Raspberry Pi.
   - `meta-qt6` – Adds Qt6 support.
   - `meta-openembedded` – Contains additional software packages.

### 6. **Recipe (`.bb` File)**
A BitBake script that defines how to build a package, specifying source location, dependencies, and installation steps.

### 7. **`.bbappend` File**
Extends or modifies an existing `.bb` recipe without changing the original file.

### 8. **Distro Configuration (`DISTRO`)**
Defines distribution-specific settings, like system initialization (`systemd` or `sysvinit`) and additional features.

### 9. **Machine Configuration (`MACHINE`)**
Specifies the target hardware platform (e.g., `raspberrypi3-64`).

### 10. **Image (`core-image`)**
A full OS image generated from recipes and configurations.

---

## Yocto Variables Summerized 🫒

### 1. **Core Variables**
| Variable | Description |
|----------|------------|
| `DISTRO` | Specifies the distribution (e.g., `poky`, `mydistro`). |
| `MACHINE` | Defines the target hardware (e.g., `raspberrypi3-64`). |
| `BBPATH` | Search path for BitBake files. |
| `BBFILES` | Lists BitBake recipe files to be parsed. |

### 2. **Build Configuration Variables**
| Variable | Description |
|----------|------------|
| `BBLAYERS` | Lists active meta-layers in `bblayers.conf`. |
| `SSTATE_DIR` | Cache directory for shared state (`sstate-cache`). |
| `DL_DIR` | Directory for downloaded sources (`downloads`). |
| `TMPDIR` | Temporary directory for builds (`tmp`). |
| `DEPENDS` | Build-time dependencies for a recipe. |
| `RDEPENDS:${PN}` | Runtime dependencies of a package. |

### 3. **Recipe-Specific Variables**
| Variable | Description |
|----------|------------|
| `SRC_URI` | URL of the source code repository or tarball. |
| `SRCREV` | Git commit hash or revision of the source. |
| `WORKDIR` | Working directory for the build process. |
| `S` | Source directory inside `WORKDIR`. |
| `B` | Build directory inside `WORKDIR`. |
| `D` | Destination directory where files are installed before packaging. |
| `bindir` | Directory for binary executables (`/usr/bin`). |
| `systemd_unitdir` | Directory for systemd service files (`/lib/systemd/system`). |

### 4. **BitBake Execution Variables**
| Variable | Description |
|----------|------------|
| `EXTRA_OECMAKE` | Extra CMake options when building a package. |
| `INHERIT` | Instructs BitBake to use predefined build classes (`qt6-cmake`, `core-image`). |
| `IMAGE_FEATURES` | Enables features in an image (e.g., `splash`, `ssh-server-dropbear`). |
| `IMAGE_INSTALL` | List of packages to install in the final image. |


# Meta-Qt6-Custom Layer Overview 🫒 

## Introduction
This Yocto layer, `meta-Qt6-custom`, is designed to create a custom Linux image with a Qt6-based application. It integrates systemd for service management and is structured to maintain modularity and compatibility.

## Directory Structure

```
meta-Qt6-custom/
├── conf
│   ├── distro
│   │   └── mydistro.conf
│   └── layer.conf
├── COPYING.MIT
├── README
├── recipes-core
│   └── images
│       └── my-core-image.bb
├── recipes-qt
│   └── myqt
│       ├── files
│       │   └── myqt.service
│       ├── myqt.bb
│       └── myqt.bbappend
```


## File Descriptions

### `conf/distro/mydistro.conf`
- Defines the custom Linux distribution.
- Extends `poky.conf` and enables `systemd` instead of `sysvinit`.
- Adds `DISTRO_FEATURES:append = " systemd pam"` to enable systemd and PAM.

### `conf/layer.conf`
- Configures the layer’s dependencies and compatibility.
- Ensures `meta-Qt6-custom` depends on `core`, `qt6-layer`, `openembedded-layer`, `meta-python`, and `raspberrypi`.

### `recipes-core/images/my-core-image.bb`
- Defines the custom image to be built.
- Inherits `core-image` and specifies installed packages.
- Installs `Qt6`, `Wayland`, `Weston`, and the custom `myqt` application.

### `recipes-qt/myqt/myqt.bb`
- Recipe to fetch and build the Qt6 application.
- Fetches source from GitHub (`SRC_URI`).
- Uses `qt6-cmake` for compilation.
- Installs the `qt-gpio-app` binary.

### `recipes-qt/myqt/myqt.bbappend`
- Extends `myqt.bb` to integrate systemd.
- Installs the `myqt.service` file.
- Enables systemd service using `SYSTEMD_AUTO_ENABLE = "enable"`.

### `recipes-qt/myqt/files/myqt.service`
- Defines a systemd service to start `qt-gpio-app` after `weston.service`.
- Ensures the application runs in a Wayland environment.
- Uses `User=weston` to ensure proper access.

## Summary
- This Yocto layer builds a custom Qt6-enabled image.
- It ensures proper Wayland integration and automatic application startup.
- Uses systemd for process management.

---
