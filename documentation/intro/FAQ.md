# Frequently Asked Questions

## Problems Running Simulations

Q. I get the following error when trying to run a simulation
```Error initializing QuantumRenderer: no suitable pipeline found
java.lang.RuntimeException: java.lang.RuntimeException: Error initializing QuantumRenderer: no suitable pipeline found
at com.sun.javafx.tk.quantum.QuantumRenderer.getInstance(QuantumRenderer.java:283)
at com.sun.javafx.tk.quantum.QuantumToolkit.init(QuantumToolkit.java:253)
at com.sun.javafx.tk.Toolkit.getToolkit(Toolkit.java:263)
at com.sun.javafx.application.PlatformImpl.startup(PlatformImpl.java:290)
at com.sun.javafx.application.PlatformImpl.startup(PlatformImpl.java:162)
at com.sun.javafx.application.LauncherImpl.startToolkit(LauncherImpl.java:651)
at com.sun.javafx.application.LauncherImpl.launchApplication1(LauncherImpl.java:671)
at com.sun.javafx.application.LauncherImpl.lambda$launchApplication$2(LauncherImpl.java:196)
at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.RuntimeException: Error initializing QuantumRenderer: no suitable pipeline found
at com.sun.javafx.tk.quantum.QuantumRenderer$PipelineRunnable.init(QuantumRenderer.java:95)
at com.sun.javafx.tk.quantum.QuantumRenderer$PipelineRunnable.run(QuantumRenderer.java:125)
... 1 more
Exception in thread "main" java.lang.RuntimeException: No toolkit found
at com.sun.javafx.tk.Toolkit.getToolkit(Toolkit.java:275)
at com.sun.javafx.application.PlatformImpl.startup(PlatformImpl.java:290)
at com.sun.javafx.application.PlatformImpl.startup(PlatformImpl.java:162)
at com.sun.javafx.application.LauncherImpl.startToolkit(LauncherImpl.java:651)
at com.sun.javafx.application.LauncherImpl.launchApplication1(LauncherImpl.java:671)
at com.sun.javafx.application.LauncherImpl.lambda$launchApplication$2(LauncherImpl.java:196)
at java.base/java.lang.Thread.run(Thread.java:1583)
```

A. Errors referring to "toolkits" and "pipelines" come from the JavaFX runtime component. Whilst most Java code can be compiled and run on different architectures, the JavaFX libraries need access to libraries specific to the platform they are being run on. The above error is caused by running an application on a Windows x64 desktop which was compiled for a Linux x64 system.