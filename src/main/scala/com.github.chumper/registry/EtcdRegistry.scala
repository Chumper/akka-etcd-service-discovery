package com.github.chumper.registry

import java.net.{DatagramSocket, InetAddress, NetworkInterface}
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern
import com.github.chumper.actor.ServiceRegistryActor
import com.github.chumper.etcd.Etcd

import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble
import scala.language.postfixOps

/**
  * Trait that offers methods to register the service under a given name, internally ot will also create an actor for
  * the registration that will also keep a lease to the set keys which will expire when the node shuts down.
  */
class EtcdRegistry(etcd: Etcd)(implicit actorSystem: ActorSystem) {

  /**
    * Will register the current network address and the given port with the service name
    * @param service the name of the service that the address should be registered under
    * @param port the port of the service
    * @return a registration that can be canceled
    */

  def register(service: String, port: Int): Registration = {
    // start actor and return an actor ref to interact with
    val ref = actorSystem.actorOf(ServiceRegistryActor.props(etcd, service, ip, port), s"$service-discovery-actor-${UUID.randomUUID().toString}")
    Registration(ref)
  }

  /**
    * Will register the given address and port with the service name
    * @param service the name of the service that the address should be registered under
    * @param address the address of the service
    * @param port the port of the service
    * @return a registration that can be canceled
    */
  def register(service: String, address: String, port: Int): Registration = {
    // start actor and return an actor ref to interact with
    val ref = actorSystem.actorOf(ServiceRegistryActor.props(etcd, service, address, port), s"$service-discovery-actor-${UUID.randomUUID().toString}")
    Registration(ref)
  }

  /**
    * get all services with the given name
    * @param serviceName the name of the service, acts as prefix
    * @return a list of addresses for the given service
    */
  def get(serviceName: String): Seq[InetAddress] = {
    Seq()
  }

  /**
    * Will watch on the given prefix service name and call the given callback when the addresses change.
    * The list is the currently up to date list with the service adresses
    * @param serviceName the service name to watch on, acts as prefix
    * @param callback the callback that should be called when the services update
    */
  def watch(serviceName: String)(callback: Seq[InetAddress] => Unit): Unit = {

  }

  /**
    * The ip adress of this system so we can add it to the registry
    */
  private val ip = {

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
}

object EtcdRegistry {

  /**
    * The prefix for the service keys
    */
  val PREFIX: String = "akka-etcd-discovery."

  def apply(etcd: Etcd)(implicit actorSystem: ActorSystem): EtcdRegistry = new EtcdRegistry(etcd)
}

/**
  * A registration class that will be returned when registering a service. Can be used to cancel the
  * registration
  * @param actorRef the actor that handles the current registration
  * @param actorSystem the system that spawned the service registration actor
  */
class Registration(actorRef: ActorRef)(implicit actorSystem: ActorSystem) {
  // will be returned to the caller so the caller can cancel the registration if needed
  def cancel(): Future[Boolean] = {
    // will stop the actor and therefore the registration
    pattern.gracefulStop(actorRef, 10 seconds)
  }
}

object Registration {
  def apply(actorRef: ActorRef)(implicit actorSystem: ActorSystem): Registration = new Registration(actorRef)
}