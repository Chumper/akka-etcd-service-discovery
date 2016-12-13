package com.github.chumper

import akka.actor.{Actor, Props}
import io.grpc.stub.AbstractStub

/**
  * Will hold a up to date list of requested services if they can be found in the registry
  */
class ServiceRegistryActor(stub: AbstractStub[AbstractStub]) extends Actor {

  stub.getChannel.


  override def receive: Receive = {
    case _ =>
  }
}

object ServiceRegistryActor {
  def props() = Props(new ServiceRegistryActor())
}