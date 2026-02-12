# Platform Specific Installation Files

These provide both the AI Sandbox application and a compatible JVM to run it.

- Windows - [AISandbox-Server-2.0.0-RC2-win-install.exe](https://files.aisandbox.dev/AISandbox-Server-2.0.0-RC2-win-install.exe)
- Linux - [AISandbox-Server-2.0.0-RC2-linux-install.sh](https://files.aisandbox.dev/AISandbox-Server-2.0.0-RC2-linux-install.sh)

# Platform Specific Java Applications

These are archives of the Java application, but without a JVM. You will need to download an appropriate runtime environment (Java 21+), we recommend downloading one from [Adoptium](https://adoptium.net/).

- Windows - [AISandbox-Server-2.0.0-RC2-win.zip](https://files.aisandbox.dev/AISandbox-Server-2.0.0-RC2-win.zip)
- Linux - [AISandbox-Server-2.0.0-RC2-linux.tar](https://files.aisandbox.dev/AISandbox-Server-2.0.0-RC2-linux.tar)
- Mac - [AISandbox-Server-2.0.0-RC2-mac.tar](https://files.aisandbox.dev/AISandbox-Server-2.0.0-RC2-mac.tar)

# Compiling from source

The full source code, which you can examine and compile yourself.

- Release [2.0.0-RC2](https://github.com/graham-evans/AISandbox-Server/releases/tag/2.0.0-RC2)

See the [compiling](../dev/Compiling.md) for instructions on how to build and launch the application.

# Legacy Versions

The first version of the Sandbox relied on a REST based interface (documented [here](../legacy/index.md)) although depreciated, you can still download binaries.

- [AISandbox_windows-x64_1_2_1.exe](https://files.aisandbox.dev/AISandbox_windows-x64_1_2_1.exe) Windows executable installer (includes JDK)
- [AISandbox_unix_1_2_1.sh](https://files.aisandbox.dev/AISandbox_unix_1_2_1.sh) - Linux executable installer (includes JDK)
- [AISandbox-Client-1.2.1.jar](https://files.aisandbox.dev/AISandbox-Client-1.2.1.jar) - Cross platform java package (requires JDK)