package com.github.chumper.etcd

import java.net.URI

import akka.actor.ActorSystem
import io.grpc.{Attributes, NameResolver}

import scala.language.postfixOps

/**
  * Factory that will resolve the EtcdNameResolver
  */
class EtcdNameResolverFactory(etcd: Etcd)(implicit val actorSystem: ActorSystem) extends NameResolver.Factory {

  override def newNameResolver(uri: URI, params: Attributes): NameResolver = {
    EtcdNameResolver(uri.getAuthority)(actorSystem, etcd)
  }

  override def getDefaultScheme: String = "etcd"
}

object EtcdNameResolverFactory {
  implicit val actorSystem: ActorSystem = ActorSystem.create("EtcdRegistry")
}