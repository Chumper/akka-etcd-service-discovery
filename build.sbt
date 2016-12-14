name := "akka-etcd-service-discovery"
version := "1.0"
scalaVersion := "2.12.0"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.14"

libraryDependencies += "com.github.Chumper" % "etcd3-scala" % "master-SNAPSHOT"