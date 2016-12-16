package com.github.chumper.actor

import java.net.InetAddress

import akka.actor.{Actor, Cancellable, Props}
import com.github.chumper.actor.ServiceRegistryActor.UpdateLease
import com.github.chumper.etcd.Etcd
import com.typesafe.scalalogging.Logger

import scala.concurrent.Await
import scala.concurrent.duration.DurationDouble
import scala.language.postfixOps

/**
  * Will insert a given service into the registry and will keep it alive as long as the actor lives
  */
class ServiceRegistryActor(etcd: Etcd, serviceName: String, port: Int) extends Actor {

  import context.dispatcher

  /**
    * schedule a keep alive for 5 seconds
    */
    val tick: Cancellable = context.system.scheduler.schedule(5 seconds, 5 seconds, self, UpdateLease)

  /**
    * The lease if any is available for this actor
    */
  var lease: Option[Long] = None

  /**
    * The ip adress of this system so we can add it to the registry
    */
  val ip: String = InetAddress.getLocalHost.getHostAddress

  /**
    * log instance
    */
  val logger: Logger = Logger[ServiceRegistryActor]

  /**
    * Will send a keep alive to the etcd
    */
  def updateLease(): Unit = {
    // update lease
    lease match {
      case None =>
      case Some(leaseId) => {
        etcd.lease.keepAlive(leaseId)
        logger.info(s"Renewed lease ($leaseId) for $serviceName")
      }
    }
  }

  override def receive: Receive = {
    // we are not acting on any custom commands except the lease update
    case UpdateLease => updateLease()
    case _ =>
  }

  override def preStart(): Unit = {
    // insert key, add lease for 10 seconds
    etcd.lease.grant(10) map { resp =>
      lease = Some(resp.iD)
      // add key with lease

      Await.result(
        etcd.kv.putString(
          key = s"akka-etcd-discovery.$serviceName.${resp.iD}",
          value = s"$ip:$port",
          lease = resp.iD
        ),
        3 seconds
      )
    }
  }

  override def postStop(): Unit = {
    // cancel periodic scheduling
    tick.cancel()
  }
}

object ServiceRegistryActor {

  sealed private case class UpdateLease()

  // used to update the lease of the registration

  def props(etcd: Etcd, service: String, port: Int) = Props {
    new ServiceRegistryActor(etcd, service, port)
  }
}