package com.github.chumper.etcd

import akka.actor.ActorSystem
import com.github.chumper.registry.EtcdRegistry
import com.spotify.docker.client.DefaultDockerClient
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.AsyncFunSuite
import util.EtcdService

import scala.concurrent.ExecutionContext

/**
  * Will test if services can be registered as expected
  */
class RegistryTest extends AsyncFunSuite with DockerTestKit with EtcdService {

  /**
    * Implicit value if there is no actorSystem in the scope
    */
  implicit val actorSystem: ActorSystem = ActorSystem.create("EtcdRegistry")

  implicit val executor: ExecutionContext = ExecutionContext.fromExecutor(null)

  test("A single service can be registered") {
    val registry = EtcdRegistry(Etcd())

    val registration = registry.register("foobar.service", 8080)
    assert(registration !== null)
  }

  test("A single service can be registered and canceled") {
    val registry = EtcdRegistry(Etcd())

    val registration = registry.register("foobar.service", 8080)
    assert(registration !== null)

    registration.cancel() map { canceled =>
      assert(canceled)
    }
  }

  override implicit def dockerFactory: DockerFactory =
    new SpotifyDockerFactory(DefaultDockerClient.fromEnv().build())
}
