package pl.stswn.tapir.playground

import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import java.time.LocalDateTime

object Playground {

  // Endpoint[I, E, O, -R]
  // I - type of input channel
  // E - type of error channel
  // O - type of output channel
  // R - additional capabilities

  // Most basic endpoint of them all (probably quite useless)
  // it will run on the root path
  // it can fail or succeed without any additional error info
  val theMostBasicEndpoint: Endpoint[Unit, Unit, Unit, Any] =
  endpoint
    .in("")
    .get

  // Infallible endpoint - cannot fail :)
  val timerEndpoint: Endpoint[Unit, Nothing, LocalDateTime, Any] =
    infallibleEndpoint
      .in("timer")
      .get
      .out(jsonBody[LocalDateTime])

  // Endpoint inputs: path segments
  case class Dog(id: Long, color: String, age: Int)
  val getDogsEndpoint: Endpoint[String, Unit, List[Dog], Any] =
    endpoint
      .in("races" / path[String] / "dogs")
      .out(jsonBody[List[Dog]])

  // Combining endpoints + query parameters
  val betterGetDogsEndpoint: Endpoint[(String, Int), Unit, List[Dog], Any] =
    getDogsEndpoint
      .in(query[Int]("age").validate(Validator.max(20)).description("Age of a dog"))

  // Structuring endpoint inputs
  case class Paging(limit: Option[Int], offset: Option[Int])
  val paging: EndpointInput[Paging] =
    query[Option[Int]]("limit")
      .and(query[Option[Int]]("offset"))
      .map[Paging]((Paging.apply _).tupled)(p => Paging.unapply(p).get)

  // Different methods
  // POST
  val postDogEndpoint: Endpoint[(String, Dog), Unit, Unit, Any] =
  endpoint
    .in("races" / path[String] / "dogs")
    .in(jsonBody[Dog])
    .post
    .out(statusCode(StatusCode.Ok))
  // DELETE
  val deleteDogEndpoint: Endpoint[(String, Long), Unit, Unit, Any] =
    endpoint
      .in("races" / path[String] / "dogs" / path[Long])
      .delete
      .out(statusCode(StatusCode.Ok))

  // Error Handling
  // explicit
  val deleteDogWithErrorHandling: Endpoint[(String, Long), StatusCode, Unit, Any] =
  deleteDogEndpoint
    .errorOut(statusCode)
  // auto handling
  sealed trait DogException
  case class DogNotFoundException(dogId: Long) extends DogException
  case object ImmortalDogException extends DogException
  val otherDeleteDogEndpoint: Endpoint[(String, Long), DogException, Unit, Any] =
    deleteDogEndpoint
      .errorOut(
        oneOf[DogException](
          oneOfMapping(StatusCode.NotFound, jsonBody[DogNotFoundException]),
          oneOfMapping(StatusCode.UnavailableForLegalReasons, emptyOutputAs(ImmortalDogException))
        )
      )

  // Many, many more
  // headers, cookies, forms, multipartForms, schema validations
  // streaming + websockets - to be presented

}