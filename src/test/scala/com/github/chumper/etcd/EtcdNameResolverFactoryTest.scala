package com.github.chumper.etcd

import java.net.URI

import etcdserverpb.rpc
import etcdserverpb.rpc.KVGrpc.KV
import io.grpc.Attributes
import org.scalatest.FunSuite

/**
  * Created by n.plaschke on 17/12/2016.
  */
class EtcdNameResolverFactoryTest extends FunSuite {

  test("Name can resolved correctly") {
    val url = s"etcd://${rpc.KVGrpc.KV.descriptor.getFullName}"
    val uri = new URI(url)

    new EtcdNameResolverFactory().newNameResolver(uri, Attributes.EMPTY)
  }

}
