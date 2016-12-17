name := "akka-etcd-service-discovery"
version := "1.0"
scalaVersion := "2.12.1"

resolvers += "jitpack" at "https://jitpack.io"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.14"
libraryDependencies += "com.github.Chumper" % "etcd3-scala" % "master-SNAPSHOT"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"