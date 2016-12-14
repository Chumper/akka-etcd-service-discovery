package com.github.chumper.actor

import akka.actor.{Actor, Props}
import com.github.chumper.etcd.Etcd

/**
  * Will hold a up to date list of requested services if they can be found in the registry
  */
class ServiceRegistryActor(etcd: Etcd, serviceName: String, port: Int) extends Actor {
  override def receive: Receive = {

  }
}

object ServiceRegistryActor {
  def props(etcd: Etcd, serviceName: String, port: Int) = Props { new ServiceRegistryActor(etcd, serviceName, port) }
}