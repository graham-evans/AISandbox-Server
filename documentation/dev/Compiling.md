# Compiling from source

AI Sandbox is written in Java, with the source code hosted on GitHub (using the GPLv3 licence). So to compile it you will need to:

1. Install a recent JDK (Java Development Kit) from Oracle or one of the OpenJDK distributions - you will need JDK version 21 or higher. Installation packages for various operating systems can be found: 

    * [The Adoptium Project from the Eclipse Foundation](https://adoptium.net/)
    * [Oracle Java](https://www.oracle.com/java/technologies/downloads)
    * [Microsoft OpenJDK build](https://learn.microsoft.com/en-us/java/openjdk/download)

2. Download the latest version of the source code from [GitHub](https://github.com/graham-evans/AISandbox-Server); use the “code” button to download.
3. Extract the directory structure to your hard drive.
4. Open a terminal / command prompt, change to the directory where you extracted the source, and type

```./gradlew run``` for Linux / Mac

or

```gradle run``` for Windows


Tips

- When compiling for the first time, Gradle will download a lot of libraries needed to compile and run the application. This may take a while, but subsequent compilations will be faster.
- The compilation will include platform specific graphics libraries (for JavaFX) so will not be cross-platform.
- Making changes to the code is far easier with an IDE that supports Gradle projects. We recommend IntelliJ IDEA or Apache Netbeans.