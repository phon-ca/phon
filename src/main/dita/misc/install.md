# Installing Phon
> Phon 3.0+ requires a 64-bit operating system.

## Install Required Fonts

Download required fronts from https://www.phon.ca/downloads/ipafonts.zip.

## Windows

 * Installer - Download the newest Phon_windows-x64_<version>.exe installer from the [releases](https://github.com/phon-ca/phon/releases) page.
 * Manual Installation - Download the Phon_windows-x64_<version>.zip package from the [releases](https://github.com/phon-ca/phon/releases) page.

##  macOS

 * Download the newest .dmg file from the [releases](https://github.com/phon-ca/phon/releases) page.  After opening the disk image, drag Phon into your Applications folder.

## Ubuntu (16.04/18.04)

 * Install Java 10
```
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt install oracle-java10-installer
```
 * Install VLC
```
sudo apt install vlc
```
 * Install Phon
Download the newest .deb from the [releases](https://github.com/phon-ca/phon/releases) page and install using dpkg.
```
sudo dpkg -i Phon_linux_<version>.deb
```
To uninstall Phon:
```
sudo dpkg --remove phon
```

## Fedora (23+)

 * Download java 10 from the [Oracle download site](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
```
wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/10.0.2+13/19aef61b38124481863b1413dce1855f/jdk-10.0.2_linux-x64_bin.tar.gz
```
 * Extract contents of Java 10 download to installation folder
```
sudo tar zxf jdk-10.0.2_linux-x64_bin.tar.gz -C /usr/local
sudo ln -s /usr/local/jdk-10.0.2 /usr/local/jdk-10
```
 * Configure system to use Java 10
```
sudo alternatives --install /usr/bin/java java /usr/local/jdk-10/bin/java 2
sudo alternatives --set java /usr/local/jdk-10/bin/java
sudo alternatives --install /usr/bin/jar jar /usr/local/jdk-10/bin/jar 2
sudo alternatives --set jar /usr/local/jdk-10/bin/jar
sudo alternatives --install /usr/bin/javac javac /usr/local/jdk-10/bin/javac 2
sudo alternatives --set javac /usr/local/jdk-10/bin/javac
```
 * Install VLC
```
sudo dnf install https://download1.rpmfusion.org/free/fedora/rpmfusion-free-release-$(rpm -E %fedora).noarch.rpm
sudo dnf install vlc
```
 * Install Phon.
Download the Phon_unix_<version>.sh installer from the [releases](https://github.com/phon-ca/phon/releases) page.
```
chmod u+x Phon_unix_<version>.sh
./Phon_unix_<version>.sh
```
or use ```sudo``` if installing for all users
```
sudo ./Phon_unix_<version>.sh
```
