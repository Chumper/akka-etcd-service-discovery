name := "akka-etcd-service-discovery"

version := "1.0"

scalaVersion := "2.12.0"

lazy val etcd = RootProject(uri("git://github.com/Chumper/etcd3-scala"))

dependsOn(etcd)

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.14"