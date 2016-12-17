package com.github.chumper.etcd

import java.net.URI

import etcdserverpb.rpc
import io.grpc.{Attributes, NameResolver}

import scala.language.postfixOps

/**
  * Factory that will resolve the
  */
class EtcdNameResolverFactory extends NameResolver.Factory {

  override def newNameResolver(uri: URI, params: Attributes): NameResolver = {
    null
  }

  override def getDefaultScheme: String = "etcd"
}
