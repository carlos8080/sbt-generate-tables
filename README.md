SBT Generate Tables - 3.1.1
==============================

This plugin adds an **SBT** task to generate ```Tables.scala``` classes automatically, for **Typesafe Slick** usage on Scala **Play** applications.

### Installation

First, add the following line in ```project/plugins.sbt``` file:

```scala
addSbtPlugin("com.eadive" % "sbt-generate-tables" % "3.1.1")
```

### Usage

To generate all classes and formats (JSON parsers) automatically, for Slick usage, run on terminal:

```scala
sbt generateTables
sbt generateJson
```

The file ```Tables.scala``` will be created on ```app/models``` folder.

### Important

The plugin uses ```default``` database, specified on ```conf/application.conf``` file.

### Revision history

Version | Changes
--------|--------
3.1.1 | Changing package name and organization to Eadive
3.1.0 | Adding generate-formats and modification to Tables.scala to accept Json formats
3.0.1 | Bugfix on Slick driver (drop last $ dollar-sign from the driver)
3.0.0 | Changing plugin to Slick 3.0.0 support
2.0.0 | Changing plugin name to sbt-generate-tables and package/organization to com.liveduca
1.0.1 | Bugfix change scala.codegen to codegen
1.0.0 | Changing from Slick 2.1.0 to Slick 3.0.0
0.1.0 | Initial public release