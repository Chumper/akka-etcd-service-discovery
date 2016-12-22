package com.github.chumper.registry

import java.net.{DatagramSocket, InetAddress, NetworkInterface}
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import com.github.chumper.actor.ServiceRegistryActor
import com.github.chumper.etcd.Etcd

import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter

/**
  * Trait that offers methods to register the service under a given name, internally ot will also create an actor for
  * the registration that will also keep a lease to the set keys which will expire when the node shuts down.
  */
class EtcdRegistry(address: String = "localhost", port: Int = 2379)(implicit actorSystem: ActorSystem) {

  val etcd = Etcd(address, port)

  def register(service: String, port: Int): ActorRef = {
    // start actor and return an actor ref to interact with
    actorSystem.actorOf(ServiceRegistryActor.props(etcd, service, ip(), port), s"$service-discovery-actor-${UUID.randomUUID().toString}")
  }

  def register(service: String, address: String, port: Int): ActorRef = {
    // start actor and return an actor ref to interact with
    actorSystem.actorOf(ServiceRegistryActor.props(etcd, service, address, port), s"$service-discovery-actor-${UUID.randomUUID().toString}")
  }

  /**
    * The ip adress of this system so we can add it to the registry
    */
  def ip(): String = {

    val enumeration = NetworkInterface.getNetworkInterfaces.asScala.toSeq

    val ipAddresses = enumeration.flatMap(p =>
      p.getInetAddresses.asScala.toSeq
    )

    val address = ipAddresses.find { address =>
      val host = address.getHostAddress
      host.contains(".") && !address.isLoopbackAddress
    }.getOrElse(InetAddress.getLocalHost)

    address.getHostAddress
  }

  def ip2(): String = {
    try {
      val socket: DatagramSocket = new DatagramSocket()
      socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
      socket.getLocalAddress.getHostAddress
    } finally {

    }
  }
}

object EtcdRegistry {

  /**
    * The prefix for the service keys
    */
  val PREFIX: String = "akka-etcd-discovery."

  /**
    * Implicit value if there is no actorSystem in the scope
    */
  implicit val actorSystem: ActorSystem = ActorSystem.create("EtcdRegistry")

  def apply(address: String = "localhost", port: Int = 2379)(implicit actorSystem: ActorSystem): EtcdRegistry = new EtcdRegistry(address, port)
}
