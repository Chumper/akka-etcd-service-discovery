package com.github.chumper.etcd

import akka.actor.ActorSystem
import io.grpc.NameResolver
import io.grpc.NameResolver.Listener

class EtcdNameResolver(serviceName: String)(implicit actorSystem: ActorSystem, etcd: Etcd) extends NameResolver {

  override def shutdown(): Unit = {
    // stop watching the keys
  }

  override def getServiceAuthority: String = ???

  override def start(listener: Listener): Unit = {
    // watch the keys
  }

}

object EtcdNameResolver {
  def apply(serviceName: String)(implicit actorSystem: ActorSystem, etcd: Etcd): EtcdNameResolver = new EtcdNameResolver(serviceName)
}