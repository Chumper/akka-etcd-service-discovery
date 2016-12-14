package com.github.chumper

import com.github.chumper.etcd.Etcd

/**
  * Will hold a up to date list of requested services if they can be found in the registry
  */
class ServiceRegistryActor(etcd: Etcd, serviceName: String, port: Int) extends Actor {


}

object ServiceRegistryActor {
}