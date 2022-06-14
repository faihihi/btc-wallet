package btc

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.actor.typed.ActorSystem
import btc.config.HttpSettings
import btc.model.BTCTransaction
import btc.model.GetHistoriesRequest
import btc.services.TransactionService
import com.typesafe.scalalogging.LazyLogging

class Routes(
    transactionHandler: TransactionService,
    httpSettings: HttpSettings
)(implicit val system: ActorSystem[_])
    extends LazyLogging {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  implicit private val timeout = httpSettings.timeout

  val saveTransactionRoute: Route =
    pathPrefix("save") {
      pathEnd {
        post {
          entity(as[BTCTransaction]) { request =>
            onSuccess(transactionHandler.saveTransaction(request))(complete(_))
          }
        }
      }
    }

  val getHistoriesRoutes: Route =
    pathPrefix("get") {
      pathEnd {
        post {
          entity(as[GetHistoriesRequest]) { request =>
            onSuccess(transactionHandler.getTransactionHistories(request))(
              complete(_)
            )
          }
        }
      }
    }

  val routes: Route = pathPrefix("wallet") {
    saveTransactionRoute ~ getHistoriesRoutes
  }
}
