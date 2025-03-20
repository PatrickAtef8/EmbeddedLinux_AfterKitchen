# Qt6 SDK and Yocto for Raspberry Pi 3B+

## Introduction

Using an SDK (Software Development Kit) with Yocto allows for efficient development and deployment of Qt6 applications on embedded systems like the Raspberry Pi 3B+. Instead of building the entire system each time, developers can cross-compile applications on a host machine and transfer them via networking (Ethernet or SSH) to the Raspberry Pi, avoiding the need to physically update the SD card.

This guide covers setting up the Qt6 SDK with Yocto, compiling Qt applications, and deploying them to a Raspberry Pi 3B+.

---

## 1. Setting Up the Qt6 SDK

### Step 1: Building the SDK
To generate the Qt6 SDK using Yocto, navigate to your Yocto build directory and run:

```sh
bitbake meta-toolchain-qt6
```

> **Note:** This process can take up to **3 hours** depending on system resources.

Once completed, navigate to the SDK directory:

```sh
cd /home/patrick/Patrick_Storage/yocto/build-rpi3b/tmp/deploy/sdk
```

### Step 2: Extracting the SDK
Run the following command to extract the SDK:

```sh
./mydistro-glibc-x86_64-meta-toolchain-qt6-cortexa53-raspberrypi3-64-toolchain-4.0.24.sh
```

Choose a suitable directory for extraction when prompted.

### Step 3: Configuring the SDK Environment
Navigate to the extracted SDK directory and source the environment setup script:

```sh
source environment-setup-cortexa53-rpi-linux
```

To confirm the SDK is correctly set up, check the compiler variable:

```sh
echo $CC
```

Expected output:

```
aarch64-rpi-linux-gcc -mcpu=cortex-a53 -march=armv8-a+crc -mbranch-protection=standard ...
```

---

## 2. Building the Qt Application

### Step 1: Clone the GitHub Repository
Navigate to your local Qt application repository you are using in your image:

```sh
git clone git@github.com:PatrickAtef8/Qt6-App-Yocto.git
cd Qt6-App-Yocto
```

Example project structure of mine:
```
.
├── CMakeLists.txt
├── images
│   ├── car2off.png
│   ├── car2on.png
│   └── itilogo.png
├── inc
│   └── controller.h
├── main.qml
├── README.md
├── resources.qrc
└── src
    ├── controller.cpp
    └── main.cpp
```

### Step 2: Compile for Raspberry Pi
Run the following commands to build the application using CMake:

```sh
cmake -S . -B buildsdk
```
![build](Result_Example/01.png)
```sh
cmake -- build buildsdk
```
![compile](Result_Example/02.png)
This generated the **qt-gpio-app** executable for Raspberry Pi.

---

## 3. Deploying the Application to Raspberry Pi

### Step 1: Configure Network for File Transfer

#### Set IP on PC
Find your network interface using:
```sh
ifconfig
```

Set a static IP for your PC:
```sh
sudo ip addr add 192.168.2.1/24 dev <your-network-interface>
```

#### Set IP on Raspberry Pi
Boot the Raspberry Pi, open a terminal, and switch to root:

```sh
su -
```

Check available interfaces:
```sh
ip a
```

Assign an IP address to `eth0`:
```sh
ip addr add 192.168.2.2/24 dev eth0
```

#### Verify Connectivity
On Raspberry Pi:
```sh
ping 192.168.2.1
```

From PC: 
```sh
ping 192.168.2.2
```

### Step 2: Transfer the Application
Once connected, navigate to the build directory on your PC and use SCP to transfer the file:

```sh
scp buildsdk/qt-gpio-app root@192.168.2.2:/home/root
```
![scp](Result_Example/03.png)


---

## 4. Running the Application on Raspberry Pi

### Step 1: Set Environment Variables
Before running the application, set the required Qt environment variables (**Check /run/user it may be not 1000**)

```sh
export XDG_RUNTIME_DIR="/run/user/1000"
export WAYLAND_DISPLAY="wayland-1"
export QT_QPA_PLATFORM="wayland"
export QT_PLUGIN_PATH="/usr/lib/plugins"
```

### Step 2: Make Variables Persistent
To ensure these variables persist after reboot, add them to **.bashrc** and **.profile**:

```sh
echo 'export XDG_RUNTIME_DIR="/run/user/1000"' >> /home/root/.bashrc
echo 'export WAYLAND_DISPLAY="wayland-1"' >> /home/root/.bashrc
echo 'export QT_QPA_PLATFORM="wayland"' >> /home/root/.bashrc
echo 'export QT_PLUGIN_PATH="/usr/lib/plugins"' >> /home/root/.bashrc
```

Also, update `.profile` to load `.bashrc`:

```sh
echo 'if [ -f ~/.bashrc ]; then' >> /home/root/.profile
echo '    . ~/.bashrc' >> /home/root/.profile
echo 'fi' >> /home/root/.profile
```

Reboot the Raspberry Pi to apply changes:
```sh
reboot
```

### Step 3: Run the Application
After reboot, SSH into the Raspberry Pi and execute the application:

```sh
cd /home/root
./qt-gpio-app
```

---
## Example Result
if I need to modify for example button color just do it like this 🕺
![result](Result_Example/res.jpeg)
## Conclusion

You have now successfully set up the Qt6 SDK, built your Qt application, and deployed it to the Raspberry Pi 3B+ over Ethernet. This approach avoids the need to modify the SD card frequently and allows for a streamlined development process using SSH and SCP for file transfers.

---

## References
- Yocto Project Documentation: [https://www.yoctoproject.org/docs/](https://www.yoctoproject.org/docs/)
- Qt Documentation: [https://doc.qt.io/](https://doc.qt.io/)
- SCP Command Usage: [https://linux.die.net/man/1/scp](https://linux.die.net/man/1/scp)
