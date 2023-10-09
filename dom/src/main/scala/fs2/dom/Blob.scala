package fs2.dom

import org.scalajs.dom
import scala.scalajs.js
import cats.effect._
import cats.syntax.all._

import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.ArrayBuffer


@JSGlobal
@js.native
class Blob[F[_]] protected () extends js.Any

object Blob {

  def apply(parts: js.Iterable[dom.BlobPart], options: dom.BlobPropertyBag): Blob[IO] =
    (new dom.Blob(parts, options))

  implicit def ops[F[_]](blob: Blob[F]): Ops[F] = new Ops(blob.asInstanceOf[dom.Blob])

  private[dom] implicit def toJS[F[_]](blob: Blob[F]): dom.Blob =
    blob.asInstanceOf[dom.Blob]
  private[dom] implicit def fromJS[F[_]](blob: dom.Blob): Blob[F] =
    blob.asInstanceOf[Blob[F]]

  final class Ops[F[_]] private[Blob] (private val blob: dom.Blob) {
    def arrayBuffer(implicit F: Async[F]): F[ArrayBuffer] = F.fromPromise(F.delay(blob.arrayBuffer()))
    def text(implicit F: Async[F]): F[String] = F.fromPromise(F.delay(blob.text()))
    def `type`(implicit F: Async[F]): F[String] = F.delay(blob.`type`)
  }

}


