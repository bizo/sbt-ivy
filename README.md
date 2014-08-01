The main added value of this plugin is to use properties files to replace placeholders in your _ivy.xml_ before SBT parses it.

##Features

If you don't care about property placeholder replacement, you won't need this plugin. Please check [Ivy settings activation](http://www.scala-sbt.org/0.13/docs/Library-Management.html#External+Maven+or+Ivy) to know how to use ivy configuration files with SBT.

 1. you can define properties that will replace any placeholder defined in _ivy.xml_. The placeholder has to follow the format "${variable.name}" with the value being defined in your _project.properties_ or _build.properties_ (the later overriding the former).
 2. placeholders can be nested. This means that your property file (_project.properties_ or _build.properties_)      can contain properties under the format

    <pre>
    prop1=value1 - ${prop2}
    prop2=value2
    </pre>

    where "${prop1}" will be resolved to "value1 - value2" in your _ivy.xml_.

##Pre-requisites

- a global _ivyconf.xml_ containing Ivy settings is accessible locally.
- the directory where to find _ivyconf.xml_ must be defined as a property in a _project.properties_ located in your project root. The file must define the property _common.build.dir_, e.g.:

<pre>
# common.build.dir contains the ivyconf.xml containing the Ivy settings
common.build.dir=/Users/me/git/common-build
</pre>

- your _ivy.xml_ must define a "sbt-test" configuration which will extend your default, compile and test configurations.
- \[optional\] if you use JUnit for your tests, you might want to add the [junit-interface](https://github.com/sbt/junit-interface) dependency to the "sbt-test" configuration.

##How to configure SBT to compile and test your Ivy project

To use the plugin, create a _./project/plugins.sbt_ containing

<pre>
lazy val root = (project in file(".")).dependsOn(sbtivy)
lazy val sbtivy = uri("git:https://github.com/matthieus/sbt-ivy/#v1.0")
</pre>
(binaries currently not hosted, forks for customization encouraged)

Then create a sbt configuration file which can be either:

- a _build.sbt_ in your project root path containing:

<pre>
import sbtivy._ // imports the plugin content

name := "my-project"

ivyBuildSettings(".") // appends the ivy specific settings to your project
</pre>

or 

- a _./project/Build.scala_ containing:

```scala
import sbt._
import Keys._
import sbtivy._ // imports the plugin content

object build extends Build {
  lazy val myProject = Project(id = "my-project",
                         base = file("."),
                         settings = Defaults.defaultSettings ++ 
                                    ivyBuildSettings(".") // appends the ivy specific settings to your project
                       )
}
```

##SBT will now give you the ability to

- **compile:** useful for _~compile_, but _~test_ is more useful.
- **test:** this plugin was especially created to have _~test_ and _~testOnly path.to.test.class_ work.

What about running the project? Untested, but it might work.

##Fitting your specific needs

In case your ivy settings file is not called _ivyconf.xml_ or you have a set of ivy configurations that is not default, compile and test, I invite you to create your own fork.

##Bugs and Requests

Please use Github issue management tool.

##License

sbt-ivy is licensed under the terms of the [Apache Software License v2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

##Copyright

Unless otherwise noted, all source files in this repository are Copyright (C) Bizo, inc.  2014

