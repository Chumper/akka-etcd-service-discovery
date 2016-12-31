package com.github.chumper.actor

import java.net.InetAddress

import akka.actor.{Actor, Props}
import com.github.chumper.etcd.Etcd

/**
  * Responsible to watch on a given key and to represent the values as `InetAddress`
  */
class WatchActor(service: String, callback: Seq[InetAddress] => Unit)(implicit etcd: Etcd) extends Actor {

  val addresses: Seq[InetAddress] = Seq.empty

  override def receive: Receive = {
    case _ =>
  }
}

object WatchActor {
  def props(service: String, callback: Seq[InetAddress] => Unit)(implicit etcd: Etcd) = Props { new WatchActor(service, callback) }
}
