import java.io.{PrintWriter, File}
import scala.io.Source

import sbt.Keys._
import sbt._
 
/**
 * This plugin generate objects and classes for Scala-Slick usage
 */
object GenerateTables extends AutoPlugin {

  /**
   * Defines all settings/tasks that get automatically imported,
   * when the plugin is enabled
   */
  object autoImport {
    lazy val genTables        = TaskKey[Seq[File]]("generate-tables")
    lazy val modifyTables     = taskKey[Unit]("modify-tables")
    lazy val generateFormats  = taskKey[Unit]("generate-formats")
    lazy val generateJson     = taskKey[Unit]("generate-json")
  }
 
  import autoImport._
 
  /**
   * Provide default settings
   */
  override lazy val projectSettings = Seq(
    genTables <<= generateTables,
    modifyTables := modifyTablesFunction,
    generateFormats := generateFormatsFunction,
    generateJson := generateJsonFunction
  )

  def generateJsonFunction(): Unit = {
    modifyTablesFunction()
    generateFormatsFunction()
  }

  /**
   * This function generate the tables using Slick codegen plugin and put it in the right Play folder (models)
   */
  def generateTables = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
    val fullPath = new File("").getAbsolutePath + "/app/models/"
    val outputDir = new File("").getAbsolutePath + "/app/"
    toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(
      getConfiguration("slick.dbs.default.driver").dropRight(1),
      getConfiguration("slick.dbs.default.db.driver"),
      getConfiguration("slick.dbs.default.db.url"),
      outputDir,
      "models",
      getConfiguration("slick.dbs.default.db.user"),
      getConfiguration("slick.dbs.default.db.password")),
      s.log))
    val fname = fullPath + "/Tables.scala"
    Seq(file(fname))
  }

  /**
   * This function reads the 'application.conf' configuration file
   */
  def getConfiguration(key: String): String = {
    val filename = new File(".").getAbsolutePath() + "/conf/application.conf"
    var result = ""
    for (line <- Source.fromFile(filename).getLines()) {
      var index = line indexOf key
      if (index == 0) {
        result = line.drop(line.indexOf("=") + 1)
        if (result.take(1) == "\"") result = result.drop(1)
        if (result.takeRight(1) == "\"") result = result.dropRight(1)
      }
    }
    result
  }

  /**
   * Modify the Tables.scala auto-generated slick tables to save case class in a separate file
   * Slick 3.0 Json formats will only work if case classes are in separate file
   */
  def modifyTablesFunction(): Unit = {
    val originFilePath    = new File(".").getAbsolutePath + "/app/models/Tables.scala"
    val originFile        = new File(originFilePath)
    val caseClassFilePath = new File(".").getAbsolutePath + "/app/models/TableRows.scala"
    val caseClassOut      = new PrintWriter(caseClassFilePath , "UTF-8")
    val newTablesFilePath = new File(".").getAbsolutePath + "/app/models/NewTables.scala"
    val newTablesOut      = new PrintWriter(newTablesFilePath , "UTF-8")
    val newTablesFile     = new File(newTablesFilePath)
    try {
      for (line <- Source.fromFile(originFilePath).getLines()) {
        if (line.trim.startsWith("package")) {
          caseClassOut.print(line + "\n\n")
          newTablesOut.print(line + "\n\n")
        } else {
          if (line.trim.startsWith("case class")) {
            caseClassOut.print(line.trim + "\n")
          } else {
            newTablesOut.print(line + "\n")
          }
        }
      }
    } finally {
      caseClassOut.close()
      newTablesOut.close()
      originFile.delete()
      newTablesFile.renameTo(originFile)
    }
  }

  /**
   * Generates Formats.scala trait, with Json formats for all case classes and correction for timestamp
   */
  def generateFormatsFunction(): Unit = {
    val preset: Seq[String] = Seq(
      "import java.sql.Timestamp\n",
      "import play.api.libs.functional.syntax._\n",
      "import play.api.libs.json._\n\n",
      "trait Formats {\n",
      "  implicit val rds: Reads[Timestamp] = (__ \\ \"time\").read[Long].map{ long => new Timestamp(long) }\n",
      "  implicit val wrs: Writes[Timestamp] = (__ \\ \"time\").write[Long].contramap{ (a: Timestamp) => a.getTime }\n",
      "  implicit val fmt: Format[Timestamp] = Format(rds, wrs)\n\n"
    )
    val formatsFilePath = new File(".").getAbsolutePath + "/app/models/Formats.scala"
    val formatsOut      = new PrintWriter(formatsFilePath , "UTF-8")
    val caseClassFilePath = new File(".").getAbsolutePath + "/app/models/TableRows.scala"

    try {
      for (line <- Source.fromFile(caseClassFilePath).getLines()) {
        if (line.startsWith("package")) {
          formatsOut.print(line + "\n\n")
          preset.foreach(presetLine => formatsOut.print(presetLine))
        } else {
          if (line.startsWith("case class")) {
            val caseClass = line.drop("case class ".length).split("Row")(0) + "Row"
            formatsOut.print("  implicit val " + caseClass + "Format = Json.format[" + caseClass + "]\n")
          }
        }
      }
      formatsOut.print("}")
    } finally {
      formatsOut.close()
    }
  }
 
}