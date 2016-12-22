package com.github.chumper.actor

import akka.actor.{Actor, Cancellable, Props}
import com.github.chumper.actor.ServiceRegistryActor.UpdateLease
import com.github.chumper.etcd.Etcd
import com.github.chumper.registry.EtcdRegistry
import com.typesafe.scalalogging.Logger

import scala.concurrent.Await
import scala.concurrent.duration.DurationDouble
import scala.language.postfixOps

/**
  * Will insert a given service into the registry and will keep it alive as long as the actor lives
  */
class ServiceRegistryActor(etcd: Etcd, serviceName: String, address: String, port: Int) extends Actor {

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
        etcd.lease.keepAlive(leaseId).map { resp =>
          logger.info(s"Renewed lease ($leaseId) for $serviceName")
        }
      }
    }
  }

  override def receive: Receive = {
    // we are not acting on any custom commands except the lease update
    case UpdateLease => updateLease()
    case _ =>
  }

  @throws(classOf[Exception])
  override def preStart(): Unit = {
    // insert key, add lease for 10 seconds
    Await.result(etcd.lease.grant(10) map { resp =>
      logger.info(s"Granted lease (${resp.iD}) for $serviceName")
      lease = Some(resp.iD)
      // add key with lease

      Await.result(
        etcd.kv.putString(
          key = s"${EtcdRegistry.PREFIX}$serviceName.${resp.iD}",
          value = s"$address:$port",
          lease = resp.iD
        ),
        3 seconds
      )
    },
      3 seconds
    )
  }

  @throws(classOf[Exception])
  override def postStop(): Unit = {
    // cancel periodic scheduling
    tick.cancel()
    lease match {
      case None =>
      case Some(leaseId) =>
        Await.result(etcd.lease.revoke(leaseId) map { resp =>
          logger.info(s"Revoked lease ($leaseId) for $serviceName")
        },
          3 seconds
        )
    }
  }
}

object ServiceRegistryActor {

  sealed private case class UpdateLease()

  // used to update the lease of the registration

  def props(etcd: Etcd, service: String, address: String, port: Int) = Props {
    new ServiceRegistryActor(etcd, service, address, port)
  }
}