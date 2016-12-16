package com.github.chumper.registry

import akka.actor.{ActorRef, ActorSystem}
import com.github.chumper.actor.ServiceRegistryActor
import com.github.chumper.etcd.Etcd
import com.trueaccord.scalapb.grpc.{AbstractService, ServiceCompanion}
import io.grpc.stub.AbstractStub

/**
  * Trait that offers methods to register the service under a given name, internally ot will also create an actor for
  * the registration that will also keep a lease to the set keys which will expire when the node shuts down.
  */
class EtcdRegistry(address: String = "localhost", port: Int = 2379)(implicit actorSystem: ActorSystem) {

  val etcd = Etcd(address, port)

  def register(service: String, port: Int): ActorRef = {
    // start actor and return an actor ref to interact with
    actorSystem.actorOf(ServiceRegistryActor.props(etcd, service, port), s"$service-discovery-actor")
  }
}

object EtcdRegistry {
  def apply(address: String = "localhost", port: Int = 2379)(implicit actorSystem: ActorSystem): EtcdRegistry = new EtcdRegistry(address, port)
}
