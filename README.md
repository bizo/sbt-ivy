Plugin to use SBT to compile and run tests from a project using an Ant/Ivy build.

**Pre-requisites:**

- a global _ivyconf.xml_ containing Ivy settings is accessible locally.
- the directory where to find _ivyconf.xml_ must be defined as a property in a _project.properties_ located in your project root. The file must define the property _common.build.dir_, e.g.:

<pre>
common.build.dir=/Users/me/git/common-build
</pre>

**How to use it:**

Create a _plugins.sbt_ containing

<pre>
lazy val root = (project in file(".")).dependsOn(sbtivy)
lazy val sbtivy = uri("git://github.com/matthieus/sbt-ivy/#v1.0")
</pre>
(binaries currently not hosted, also note the use of the tag in the url)

Create a sbt configuration file which can be either:

- a _build.sbt_ in your project root path containing:

<pre>
import sbtivy._ // import the plugin content

name := "my-project"

ivyBuildSettings(".") // append the ivy specific settings to your project
</pre>

or 

- a _./project/Build.scala_ containing:

```scala
import sbt._
import Keys._
import sbtivy._ // import the plugin content

object build extends Build {
  lazy val myProject = Project(id = "my-project",
                         base = file("."),
                         settings = Defaults.defaultSettings ++ 
                                    ivyBuildSettings(".") // append the ivy specific settings to your project
                       )
}
```

**Cool features:**

- you can define properties that will replace any placeholder defined in _ivy.xml_. The placeholder has to follow the format "${variable.name}" with the value being defined in your _project.properties_ or _build.properties_ (the later overriding the former).
- placeholders can be nested. This means that your property file (_project.properties_ or _build.properties_) can contain properties under the format

<pre>
prop1=value1 - ${prop2}
prop2=value2
</pre>

where "${prop1}" will resolve to "value1 - value2" in your _ivy.xml_.

**SBT will now give you the ability to**

- compile: useful for _~compile_, but _~test_ is more useful.
- test: this plugin was especially created to have _~test_ or _~testOnly path.to.test.class_ working.

What about running the project? Untested, but it might work.

**Fitting your specific needs**

In case your ivy settings file is not called _ivyconf.xml_ for example, I invite you to create your own fork.
