package com.github.chumper.actor

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import com.github.chumper.etcd.Etcd
import com.github.chumper.registry.EtcdRegistry
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
  * Responsible to watch on a given key and to represent the values as `InetAddress`
  */
class WatchActor(service: String, callback: Seq[InetSocketAddress] => Unit)(implicit etcd: Etcd) extends Actor {

  val log: Logger = Logger[WatchActor]

  var addresses: Seq[InetSocketAddress] = Seq.empty

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    log.info(s"Watching service $service")
    Await.result(
      etcd.watch.prefix(EtcdRegistry.PREFIX + service) { resp =>
        if (resp.created) {
          log.info(s"$service watch created")
          getServices map { addresses => this.addresses = addresses }
          if (this.addresses.nonEmpty) {
            callback.apply(this.addresses)
          }
        } else if (resp.canceled) {
          log.info(s"$service watch canceled")
          context.stop(self)
        } else {
          log.info(s"$service updated")
          resp.events.foreach { e =>
            e.`type` match {
              case mvccpb.kv.Event.EventType.PUT =>
                e.kv match {
                  case None =>
                  case Some(kv) =>
                    val address = kv.value.toStringUtf8.split(":")
                    this.addresses = this.addresses :+ new InetSocketAddress(address(0), address(1).toInt)
                }
              case mvccpb.kv.Event.EventType.DELETE =>
                val i = 0
            }
          }
          callback.apply(this.addresses)
        }
      }, 5 seconds)
  }

  def getServices: Future[Seq[InetSocketAddress]] = {
    etcd.kv.prefix(service) map { resp =>
      resp.kvs.map { v =>
        val address = v.value.toStringUtf8.split(":")
        new InetSocketAddress(address(0), address(1).toInt)
      }
    }
  }

  override def receive: Receive = {
    case _ =>
  }
}

object WatchActor {
  def props(service: String, callback: Seq[InetSocketAddress] => Unit)(implicit etcd: Etcd) = Props {
    new WatchActor(service, callback)
  }
}
