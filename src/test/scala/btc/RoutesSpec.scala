package btc

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.util.Timeout
import btc.config.HttpSettings
import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.services.TransactionService
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.mock
import org.mockito.MockitoSugar.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Duration
import scala.concurrent.Future

class RoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with TestData {

  lazy val testKit                                         = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing]           = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  val mockTransactionHandler = mock[TransactionService]
  val httpSettings           = HttpSettings(timeout = Timeout.create(Duration.ZERO))
  lazy val routes            = new Routes(mockTransactionHandler, httpSettings)

  "Routes" should {
    "call saveTransaction successfully" in {
      val saveTransactionEntity  =
        HttpEntity(ContentTypes.`application/json`, saveTransactionRequestJson)
      val saveTransactionRequest = Post("/save", saveTransactionEntity)
      val saveTransactionRoute   = routes.saveTransactionRoute

      when(mockTransactionHandler.saveTransaction(any[BTCTransaction]))
        .thenReturn(Future.successful(saveTransactionResponse))

      saveTransactionRequest ~> saveTransactionRoute ~> check {
        status.isSuccess() shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe saveTransactionResponseJson
      }
    }

    "call getHistories successfully" in {
      val getHistoriesEntity  =
        HttpEntity(ContentTypes.`application/json`, getHistoriesRequestJson)
      val getHistoriesRequest = Post("/get", getHistoriesEntity)
      val getHistoriesRoute   = routes.getHistoriesRoutes

      when(
        mockTransactionHandler.getTransactionHistories(any[GetHistoriesRequest])
      )
        .thenReturn(Future.successful(getHistoriesResponse))

      getHistoriesRequest ~> getHistoriesRoute ~> check {
        status.isSuccess() shouldBe true
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe getHistoriesResponseJson
      }
    }
  }
}
