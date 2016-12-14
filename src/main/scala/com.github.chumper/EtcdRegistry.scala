package com.github.chumper

import com.github.chumper.etcd.Etcd
import akka.actor.ActorSystem

/**
  * Trait that offers methods to register the service under a given name, internally ot will also create an actor for
  * the registration that will also keep a lease to the set keys which will expire when the node shuts down.
  */
class EtcdRegistry(address: String = "localhost", port: Int = 2379)(implicit actorSystem: ActorSystem) {

  val etcd = Etcd(address, port)

  def register(serviceName: String, port: Int) = {

  }

}
