package pl.stswn.tapir.server

import org.http4s.syntax.all._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig}
import zio.clock.Clock
import zio.{App, ExitCode, RIO, URIO, ZEnv, ZIO}
import zio.interop.catz._

object ZIOMain extends App {
  val serve: ZIO[ZEnv with Logic, Throwable, Unit] = ZIO.runtime[ZEnv with Logic].flatMap { implicit runtime =>
    BlazeServerBuilder[RIO[Logic with Clock, *]](runtime.platform.executor.asEC)
      .bindHttp(8080, "localhost")
      .withHttpApp(CORS(Router("/" -> Routes.routes).orNotFound))
      .serve
      .compile
      .drain
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    serve.provideCustomLayer(Logic.live).exitCode
  }
}
