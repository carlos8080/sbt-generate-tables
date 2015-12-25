name := """sbt-generate-tables"""

version := "3.1.0"

scalaVersion := "2.10.5"

organization := "com.liveduca"

sbtPlugin := true

libraryDependencies ++= Seq(
  "mysql"               % "mysql-connector-java"  % "5.1.35",
  "com.typesafe.slick"  %% "slick"                % "3.0.0",
  "com.typesafe.slick"  %% "slick-codegen"        % "3.0.0",
  "org.scalatest"       %% "scalatest"            % "2.2.1" % "test"
)