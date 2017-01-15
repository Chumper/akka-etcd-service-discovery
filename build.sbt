name := "akka-etcd-service-discovery"
version := "1.0"
scalaVersion := "2.12.1"

resolvers += "jitpack" at "https://jitpack.io"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.14"
libraryDependencies += "com.github.Chumper" % "etcd3-scala" % "0.1.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.8"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "org.slf4j" % "jul-to-slf4j" % "1.7.22"
libraryDependencies += "org.codehaus.groovy" % "groovy-all" % "2.4.7"

libraryDependencies += "com.whisk" %% "docker-testkit-scalatest" % "0.9.0-RC2" % "test"
libraryDependencies += "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.0-RC2" % "test"