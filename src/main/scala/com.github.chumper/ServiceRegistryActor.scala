package com.github.chumper

import io.grpc.stub.AbstractStub

/**
  * Will hold a up to date list of requested services if they can be found in the registry
  */
class ServiceRegistryActor(stub: AbstractStub[AbstractStub]) {
}

object ServiceRegistryActor {
}