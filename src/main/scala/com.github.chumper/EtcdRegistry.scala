package com.github.chumper

/**
  * Trait that offers methods to register the service under a given name, internally ot will also create an actor for
  * the registration that will also keep a lease to the set keys which will expire when the node shuts down.
  */
trait EtcdRegistry {

}
