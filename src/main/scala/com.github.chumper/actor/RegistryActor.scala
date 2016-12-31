package com.github.chumper.actor

import java.net.InetSocketAddress
import java.util.UUID

import akka.actor.{Actor, ActorSystem, Props}
import com.github.chumper.actor.RegistryActor.{RegisterService, WatchService}
import com.github.chumper.etcd.Etcd
import com.github.chumper.registry.{Registration, Watcher}

/**
  * Root actor that will be spawned when a registry will be created
  */
class RegistryActor(implicit val etcd: Etcd, implicit val actorSystem: ActorSystem) extends Actor {

  override def receive: Receive = {
    case RegisterService(service, address, port) => register(service, address, port)
    case WatchService(service, callback) => watch(service, callback)
  }

  def register(service: String, address: String, port: Int): Unit = {
    val ref = context.actorOf(RegistrationActor.props(etcd, service, address, port), s"$service-register-actor-${UUID.randomUUID().toString}")
    sender() ! Registration(ref)
  }

  def watch(service: String, callback: Seq[InetSocketAddress] => Unit): Unit = {
    val ref = context.actorOf(WatchActor.props(service, callback), s"$service-watch-actor-${UUID.randomUUID().toString}")
    sender() ! Watcher(ref)
  }
}

object RegistryActor {

  // command classes
  case class RegisterService(service: String, address: String, port: Int)
  case class WatchService(service: String, callback: Seq[InetSocketAddress] => Unit)

  def props()(implicit etcd: Etcd, actorSystem: ActorSystem) = Props { new RegistryActor() }
}
