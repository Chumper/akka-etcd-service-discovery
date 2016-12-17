package com.github.chumper.etcd

import io.grpc.NameResolver
import io.grpc.NameResolver.Listener

class EtcdNameResolver extends NameResolver {

  override def shutdown(): Unit = ???

  override def getServiceAuthority: String = ???

  override def start(listener: Listener): Unit = ???

}
