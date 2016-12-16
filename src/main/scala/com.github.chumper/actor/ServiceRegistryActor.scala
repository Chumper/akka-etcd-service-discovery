package com.github.chumper.actor

import akka.actor.{Actor, Cancellable, Props}
import com.github.chumper.actor.ServiceRegistryActor.UpdateLease
import com.github.chumper.etcd.Etcd
import com.trueaccord.scalapb.grpc.{AbstractService, ServiceCompanion}
import io.grpc.stub.AbstractStub

import scala.concurrent.duration.DurationDouble

/**
  * Will hold a up to date list of requested services if they can be found in the registry
  */
class ServiceRegistryActor(etcd: Etcd, serviceName: String, port: Int) extends Actor {
  import context.dispatcher

  // schedule a keep alive for 5 seconds
  val tick: Cancellable = context.system.scheduler.schedule(5 seconds, 5 seconds, self, UpdateLease)

  def updateLease(): Unit = {
    // update lease
  }

  override def receive: Receive = {
    // we are not acting on any custom commands except the lease update
    case UpdateLease => updateLease()
    case _ =>
  }

  override def preStart(): Unit = {
    // insert key, add lease for 10 seconds
  }

  override def postStop(): Unit = {
    // cancel periodic scheduling
    tick.cancel()
  }
}

object ServiceRegistryActor {

  sealed private case class UpdateLease() // used to update the lease of the registration

  def props(etcd: Etcd, service: String, port: Int) = Props { new ServiceRegistryActor(etcd, service, port) }
}