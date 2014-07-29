package sbtivy

import java.io.{PrintWriter, FileInputStream, File}
import java.util.Properties
import java.util.regex.{Matcher, Pattern}
import scala.io.Source

object utils {
  def ivyBuildSettings(projectPath: String) = {
      import utils._

      val ivyXmlString = Using(Source.fromFile(s"$projectPath/ivy.xml"))(_.mkString)

      val props = {
        val commonBuildDir = readProperties(new File(s"$projectPath/project.properties"))("common.build.dir")
        val propertiesSourcePaths = Seq(s"$commonBuildDir/project.properties", s"$commonBuildDir/build.properties", s"$projectPath/project.properties", s"$projectPath/build.properties")
        val props_ = propertiesSourcePaths
          .flatMap(path => readProperties(new File(path)))
          .toMap
        val props_withBasedir = props_ + (("basedir", projectPath))

        replaceAllVariablesInProps(props_withBasedir)

      }
      val (ivyXmlUpdated, _) = replaceAllVariables(ivyXmlString, props)

      val ivyXmlUpdatedFile = {
        val tmpFile = File.createTempFile("ivy_updated_", ".tmp")
        tmpFile.deleteOnExit()
        printToFile(tmpFile)(_.print(ivyXmlUpdated))
        tmpFile
      }
      println("path of updated ivy.xml: "+ivyXmlUpdatedFile.getAbsolutePath)

      val buildScalaOptions = Seq("-unchecked", "-deprecation", "-encoding", "utf8")

      Seq(
        externalIvyFile(baseDirectory(_ => ivyXmlUpdatedFile)),
        externalIvySettings(baseDirectory(_ => new File(props("common.build.dir")+"/ivyconf.xml"))),
        scalacOptions                     := buildScalaOptions,
        classpathConfiguration in Compile := config("default") extend config("compile"),
        classpathConfiguration in Test    := config("sbt-test") extend(config("default"), config("compile"), config("test")),
        parallelExecution in Test         := false
      )
    }
  }

  // looks ahead/behind for '$'{' and '}' with a variable name in the middle
  final val variablePattern = """(?<=\$\{)[0-9a-zA-Z_\.]+(?=\})""".r

  /** Recursively replaces properties containing references to other property placeholders. */
  def replaceAllVariablesInProps(props: Map[String, String]): Map[String, String] = {
    var rerun = false
    val returned =
      props.map { case (name, value) =>
        val (resolved, ignored) = replaceAllVariables(value, props)
        if (!rerun && !(findAllVariables(resolved) -- ignored).isEmpty) rerun = true
        (name, resolved)
      }
    if (rerun) {
      replaceAllVariablesInProps(returned)
    }
    else returned
  }

  /** Replaces all the property placeholders by their value and return a tuple of the resulting string and ignored variables. */
  def replaceAllVariables(s: String, props: Map[String, String]): (String, Set[String]) = {
    findAllVariables(s)
      .foldLeft(s, Set[String]()){ case ((xml, ignored), name) =>
      props.get(name)
        .map( substitute => (substituteVariable( xml, name, substitute), ignored))
        .getOrElse((xml, ignored + name))
    }
  }

  def substituteVariable(s: String, variable: String, value: String) = {
    s.replaceAll(Pattern.quote("${"+variable+"}"), Matcher.quoteReplacement(value))
  }

  def findAllVariables(s: String): Set[String] = variablePattern.findAllIn(s).to[Set]

  def readProperties(propertiesFile: File): collection.mutable.Map[String, String] = {
    import scala.collection.JavaConverters.propertiesAsScalaMapConverter

    val properties = new Properties
    if(propertiesFile.exists)
      Using( new FileInputStream(propertiesFile) )( properties.load )
    properties.asScala
  }

  def printToFile(f: File)(op: java.io.PrintWriter => Unit) {
    Using(new PrintWriter(f)){ op }
  }

  object Using {
    import scala.language.reflectiveCalls

    def apply[R <: { def close(): Unit },T](create: R)(f: R => T): T = try { f(create) } finally { create.close() }
  }
}
