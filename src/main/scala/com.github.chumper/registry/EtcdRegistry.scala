package com.github.chumper.registry

import java.net.{InetAddress, NetworkInterface}
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern
import akka.pattern.ask
import akka.util.Timeout
import com.github.chumper.actor.RegistryActor
import com.github.chumper.actor.RegistryActor.{RegisterService, WatchService}
import com.github.chumper.etcd.Etcd

import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble
import scala.language.postfixOps

/**
  * Trait that offers methods to register the service under a given name, internally ot will also create an actor for
  * the registration that will also keep a lease to the set keys which will expire when the node shuts down.
  */
class EtcdRegistry(etcd: Etcd)(implicit actorSystem: ActorSystem) {

  /**
    * Main actor for all registrations, can be used to cancel all registrations and also has a custom supervisor
    * strategy for all child actors
    */
  var registryActor: Option[ActorRef] = Some(actorSystem.actorOf(RegistryActor.props()(etcd, actorSystem), s"registry-${UUID.randomUUID().toString}"))

  implicit val timeout = Timeout(5 seconds)

  /**
    * Will register the current network address and the given port with the service name
    * @param service the name of the service that the address should be registered under
    * @param port the port of the service
    * @return a registration that can be canceled
    */
  def register(service: String, port: Int): Future[Registration] = registryActor match {
    case None => throw new RuntimeException("Registry was shutdown")
    case Some(ar) => (ar ? RegisterService(service, ip, port)).mapTo[Registration]
  }

  /**
    * Will register the given address and port with the service name
    * @param service the name of the service that the address should be registered under
    * @param address the address of the service
    * @param port the port of the service
    * @return a registration that can be canceled
    */
  def register(service: String, address: String, port: Int): Future[Registration] = registryActor match {
    case None => throw new RuntimeException("Registry was shutdown")
    case Some(ar) => (ar ? RegisterService(service, address, port)).mapTo[Registration]
  }

  /**
    * Will watch on the given prefix service name and call the given callback when the addresses change.
    * The list is the currently up to date list with the service addresses
    * @param serviceName the service name to watch on, acts as prefix
    * @param callback the callback that should be called when the services update
    */
  def watch(serviceName: String)(callback: Seq[InetAddress] => Unit): Future[Watcher] = registryActor match {
    case None => throw new RuntimeException("Registry was shutdown")
    case Some(ar) => (ar ? WatchService(serviceName, callback)).mapTo[Watcher]
  }

  /**
    * Will shutdown the registry which will result in all registrations to be canceled
    * @return a future which will hold the value if the registry could be shutdown
    */
  def shutdown(): Future[Boolean] = registryActor match {
    case Some(ar) => pattern.gracefulStop(ar, 10 seconds).map { shutdown =>
      registryActor = None
      shutdown
    }
    case None => Future { true }
  }

  def isShutdown: Boolean = registryActor.isEmpty

  /**
    * The ip address of this system which will be detected by iterating over the network interfaces and finding an
    * interface that is not a loopbackAddress and has a valid hostname
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
  /**
    * Will stop the actor associated with this registration and will remove the registration from the registry
    * @return a future indicating if the removal was successful
    */
  def cancel(): Future[Boolean] = {
    pattern.gracefulStop(actorRef, 10 seconds)
  }
}

object Registration {
  def apply(actorRef: ActorRef)(implicit actorSystem: ActorSystem): Registration = new Registration(actorRef)
}

class Watcher(actorRef: ActorRef)(implicit actorSystem: ActorSystem) {
  def cancel(): Future[Boolean] = {
    pattern.gracefulStop(actorRef, 10 seconds)
  }
}

object Watcher {
  def apply(actorRef: ActorRef)(implicit actorSystem: ActorSystem): Registration = new Registration(actorRef)
}