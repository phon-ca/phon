# Installing Phon
> Phon 3.0+ requires a 64-bit operating system.

## Install Required Fonts

Download required fronts from https://www.phon.ca/downloads/ipafonts.zip.

## Windows

 * Installer - Download and execution the newest ```Phon_windows-x64_<version>.exe``` installer from the [releases](https://github.com/phon-ca/phon/releases) page.
 * Manual Installation - Download the ```Phon_windows-x64_<version>.zip``` package from the [releases](https://github.com/phon-ca/phon/releases) page.

##  macOS

 * Download the newest ```Phon_macos_<version>.dmg``` file from the [releases](https://github.com/phon-ca/phon/releases) page.  After opening the disk image, drag Phon into your Applications folder.

## Ubuntu (16.04/18.04)

 * Install Java 11
```
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt install oracle-java11-installer
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

## Other linux distributions

 * Downloand and install java 11 and VLC
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
