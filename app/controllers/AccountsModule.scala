package controllers

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.AccountsActor

class AccountsModule extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bindActor[AccountsActor]("accounts-actor")
  }
}