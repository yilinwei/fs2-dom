package fs2.dom

import cats.effect.kernel.Sync
import org.scalajs.dom

abstract class BlobEvent[F[_]] private[dom] extends Event[F] {
  def data: Blob[F]
}

object BlobEvent {
  def apply[F[_]](event: dom.BlobEvent)(implicit F: Sync[F]): BlobEvent[F] =
    new WrappedBlobEvent(event)
}

private final class WrappedBlobEvent[F[_]](val event: dom.BlobEvent)(implicit
    val F: Sync[F]
) extends BlobEventImpl[F]

private trait BlobEventImpl[F[_]] extends BlobEvent[F] with EventImpl[F] {
  def event: dom.BlobEvent
  def data: Blob[F] = event.data
  implicit def F: Sync[F]
}

