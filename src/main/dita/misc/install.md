# Installing Phon

## Install Required Fonts

Download required fronts from https://www.phon.ca/downloads/ipafonts.zip.

## Windows

Download the newest .exe installer file from the [releases](https://github.com/phon-ca/phon/releases) page.

> Note: Phon 3.0+ only supports 64-bit operating systems.

##  macOS

Download the newest .dmg file from the [releases](https://github.com/phon-ca/phon/releases) page.  After opening the disk image, drag Phon into your Applications folder.

## Ubuntu (16.04/18.04)

First, install the newest version of Java 10 using packages provided by linuxuprising.  

Install java 10:
```
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt install oracle-java10-installer
```

Install vlc:
```
sudo apt install vlc
```

Download the newest .deb from the [releases](https://github.com/phon-ca/phon/releases) page and install using dpkg.
```
sudo dpkg -i Phon_linux_<version>.deb
```

To uninstall Phon:
```
sudo dpkg --remove phon
```

## \*nix

On other \*nix systems install java9+ and VLC then use the .sh installer from the [releases](https://github.com/phon-ca/phon/releases) page.

e.g.,
```
chmod u+x Phon_unix_<version>.sh
./Phon_unix_<version>.sh
```

or use ```sudo``` if installing for all users

```
sudo ./Phon_unix_<version>.sh
```
