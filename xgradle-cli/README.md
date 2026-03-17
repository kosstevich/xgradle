
# xgradle-cli

**xgradle-cli** is the **standalone CLI** component of **xgradle**.

It is responsible for handling the **artifacts produced by a build that runs with xgradle-resolution-plugin**:
- discovers build outputs (JARs + POM metadata, optional javadocs),
- installs/exports them into packaging-controlled directories,
- registers artifacts and BOMs (typically via **XMvn** tooling),
- optionally **patches (redacts)** POM files to match offline/system packaging requirements.

---

## Where it fits in RPM packaging

In RPM (and similar distro packaging) workflows **xgradle-cli** is typically used in the **`%install`** section.

Its job is to take the artifacts produced by the build and **prepare them for distribution**:
place them into the correct filesystem layout, generate/adjust metadata as needed, and ensure the resulting
system artifact set is ready to be consumed offline.

---

## Artifacts

This module produces:

- `xgradle-cli.jar` — runnable CLI JAR
- `xgradle-cli` — launcher script/wrapper

---

## Quick start

Show help:

```bash
xgradle-cli --help

## Usage Example:

```bash
gradle build \
  -Djava.library.dir=/usr/share/java \
  -Dmaven.poms.dir=/usr/share/maven-poms \
  -Dgenerate.sbom=cyclonedx \
  --offline
```

```
[root@82c69e916850 plumelib-options]# xgradle-cli '--xmvn-register=/usr/bin/python3 /usr/share/java-utils/mvn_artifact.py' --searching-directory=/root/.m2
[main] INFO XGradleLogger - Processed 1 unique artifacts from 1 POM files
[main] INFO XGradleLogger - 
Registering pair: /usr/bin/python3 /usr/share/java-utils/mvn_artifact.py /root/.m2/repository/org/plumelib/options/2.0.3/options-2.0.3.pom /root/.m2/repository/org/plumelib/options/2.0.3/options-2.0.3.jar
[main] INFO XGradleLogger - Artifacts registered successfully
[root@82c69e916850 plumelib-options]# xmvn-install -R .xmvn-reactor -n plumelib-options -d /usr/src/tmp/plumelib-options-buildroot
[INFO] Installing artifact org.plumelib:options:pom:2.0.3
[INFO] Installing artifact org.plumelib:options:jar:2.0.3
[INFO] Installation successful  
[root@82c69e916850 plumelib-options]# ls /usr/src/tmp/plumelib-options-buildroot/usr/share/maven-poms/
plumelib-options
[root@82c69e916850 plumelib-options]# ls /usr/src/tmp/plumelib-options-buildroot/usr/share/maven-poms/plumelib-options/
options.pom
[root@82c69e916850 plumelib-options]# ls /usr/src/tmp/plumelib-options-buildroot/usr/share/java/plumelib-options/
options.jar
```

