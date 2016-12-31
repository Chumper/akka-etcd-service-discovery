package com.github.chumper.etcd

import akka.actor.ActorSystem
import com.github.chumper.registry.EtcdRegistry
import com.spotify.docker.client.DefaultDockerClient
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest._
import util.EtcdService

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.Try

/**
  * Will test if services can be registered as expected
  */
class RegistryTest extends AsyncFunSuite with BeforeAndAfter with DockerTestKit with EtcdService {

  /**
    * Implicit value if there is no actorSystem in the scope
    */
  implicit val actorSystem: ActorSystem = ActorSystem.create("EtcdRegistry")

  implicit val executor: ExecutionContext = ExecutionContext.fromExecutor(null)

  var registry: EtcdRegistry = _

  before {
    registry = EtcdRegistry(Etcd())
  }

  test("A single service can be registered") {
    for {
      r1 <- registry.register("foo.bar.service", 8080)
    } yield assert(r1 !== null)
  }

  test("A single service can be registered and canceled") {
    for {
      r1 <- registry.register("foo.bar.service", 8080)
      r2 <- r1.cancel()
    } yield assert(r2)
  }

  test("Two services can be registered and canceled") {
    for {
      r1 <- registry.register("foo.bar.service", 8080)
      r2 <- registry.register("foo.bar.service", 8081)
      r3 <- r1.cancel()
      r4 <- r2.cancel()
    } yield assert(r3 && r4)
  }

  test("A service can be registered and a watcher will be updated") {
    val p = Promise[Assertion]
    registry.watch("foo.bar.service") { servers =>
      assert(servers.size === 1)
      p.tryComplete(Try{assert(true)})
    }
    registry.register("foo.bar.service", 8080)
    p.future
  }

  override implicit def dockerFactory: DockerFactory =
    new SpotifyDockerFactory(DefaultDockerClient.fromEnv().build())
}
