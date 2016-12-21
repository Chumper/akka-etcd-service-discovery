package com.github.chumper.etcd

import java.net.{InetSocketAddress, SocketAddress}
import java.util.Collections

import akka.actor.ActorSystem
import com.github.chumper.registry.EtcdRegistry
import io.grpc.{Attributes, NameResolver, ResolvedServerInfo}
import io.grpc.NameResolver.Listener
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConverters.seqAsJavaListConverter

class EtcdNameResolver(serviceName: String)(implicit actorSystem: ActorSystem, etcd: Etcd) extends NameResolver {

  var watchId: Option[Long] = None

  override def shutdown(): Unit = {
    // stop watching the keys
    watchId match {
      case Some(id) => etcd.watch.cancel(id)
        watchId = None
      case None =>
    }
  }

  override def getServiceAuthority: String = serviceName

  override def start(listener: Listener): Unit = {

    def getServers = {
      etcd.kv.prefix(s"${EtcdRegistry.PREFIX}$serviceName").map { resp =>
        // generate service list
        val addresses = resp.kvs.map {
          _.value.toStringUtf8
        }
        val addresses2 = addresses.map { e =>
          val ad = e.split(":")
          new ResolvedServerInfo(new InetSocketAddress(ad(0), ad(1).toInt), Attributes.EMPTY)
        }
        println(addresses2)
        listener.onUpdate(Collections.singletonList(addresses2.asJava), Attributes.EMPTY)
      }
    }

    // watch the keys
    etcd.watch.prefix(s"${EtcdRegistry.PREFIX}$serviceName") { resp =>
      // list updated, so get the new servers
      getServers
    }
  }
}

object EtcdNameResolver {
  def apply(serviceName: String)(implicit actorSystem: ActorSystem, etcd: Etcd): EtcdNameResolver = new EtcdNameResolver(serviceName)
}