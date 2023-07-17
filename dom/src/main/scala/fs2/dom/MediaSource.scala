package fs2.dom

import org.scalajs.dom
import scala.scalajs.js
import cats.effect._
import cats.syntax.all._

// TODO: Wait for upgtade
import scala.scalajs.js.annotation.*

@js.native
@JSGlobal
private object URL extends js.Object {
  def createObjectURL(src: dom.MediaSource): String = js.native
  def revokeObjectURL(url: String): Unit = js.native
}

trait MediaSource[F[_]] extends js.Any

object MediaSource {

  implicit def ops[F[_]](node: MediaSource[F]): Ops[F] = new Ops(node.asInstanceOf[dom.MediaSource])

  private[dom] implicit def toJS[F[_]](node: MediaSource[F]): dom.MediaSource =
    node.asInstanceOf[dom.MediaSource]
  private[dom] implicit def fromJS[F[_]](node: dom.MediaSource): MediaSource[F] =
    node.asInstanceOf[MediaSource[F]]

  final class Ops[F[_]] private[MediaSource] (private val src: dom.MediaSource) {
    def readyState(implicit F: Async[F]): F[dom.MediaSourceReadyState] = F.delay(src.readyState)
    def endOfStream(implicit F: Async[F]): F[Unit] = F.delay(src.endOfStream())
    def sourceOpenOnce(implicit F: Async[F]): F[Unit] =
      EventTargetHelpers.listenOnce[F, dom.Event](src, "sourceopen").void
    def url(implicit F: Async[F]): Resource[F, String] =
      Resource.make(F.delay(URL.createObjectURL(src)))(url => F.delay(URL.revokeObjectURL(url)))
  }
}

//TODO: Upstream
@JSGlobal
@js.native
class BaseAudioContext[F[_]] extends js.Any

@JSGlobal
@js.native
class AudioContext[F[_]] extends BaseAudioContext[F]

object AudioContext {

  private[dom] implicit def fromJS[F[_]](ctx: dom.AudioContext): AudioContext[F] =
    ctx.asInstanceOf[AudioContext[F]]


  def apply[F[_]](implicit F: Async[F]): Resource[F, AudioContext[F]] =
    Resource
      .make(F.delay(new dom.AudioContext()))(new Ops[F](_).close)
      .map(_.asInstanceOf[AudioContext[F]])

  implicit def ops[F[_]](node: AudioContext[F]): Ops[F] = new Ops(
    node.asInstanceOf[dom.AudioContext]
  )

  final class Ops[F[_]] private[AudioContext] (private val ctx: dom.AudioContext) {

    def state(implicit F: Async[F]): F[String] = F.delay(ctx.state)

    def createSource(e: HtmlMediaElement[F])(implicit F: Async[F]): F[MediaElementAudioSourceNode[F]] =
      F.delay(
        ctx.createMediaElementSource(e)
      )

    def resume(implicit F: Async[F]): F[Unit] = F.fromPromise(F.delay(ctx.resume()))
    def suspend(implicit F: Async[F]): F[Unit] = F.fromPromise(F.delay(ctx.suspend()))
    def close(implicit F: Async[F]): F[Unit] = F.fromPromise(F.delay(ctx.close()))
  }
}

trait AudioNode[F[_]] extends js.Any
object AudioNode {

  private[dom] implicit def toJS[F[_]](ctx: AudioNode[F]): dom.AudioNode = ctx.asInstanceOf[dom.AudioNode]

  final class Ops[F[_]] private[AudioNode] (private val node: dom.AudioNode) extends AnyVal {
    def context(implicit F: Async[F]): F[AudioContext[F]] = F.delay(node.context)
    // FIXME: Move to base context
    def connect(destination: AudioNode[F])(implicit F: Async[F]): F[Unit] = F.delay(node.connect(destination))
    def disconnect(destination: AudioNode[F])(implicit F: Async[F]): F[Unit] = F.delay(node.disconnect(destination))
    def numberOfInputs(implicit F: Async[F]): F[Int] = F.delay(node.numberOfInputs)
    def numberOfOutputs(implicit F: Async[F]): F[Int] = F.delay(node.numberOfOutputs)
    def channelCount(implicit F: Async[F]): F[Int] = F.delay(node.channelCount)
  }
}

trait MediaElementAudioSourceNode[F[_]] extends AudioNode[F]
object MediaElementAudioSourceNode {

  private[dom] implicit def fromJS[F[_]](ctx: dom.MediaElementAudioSourceNode): MediaElementAudioSourceNode[F] =
    ctx.asInstanceOf[MediaElementAudioSourceNode[F]]

  final class Ops[F[_]] private[MediaElementAudioSourceNode] (private val node: dom.MediaElementAudioSourceNode) {
    // FIXME: Upstream
    // def mediaElement(implicit F: Async[F]): F[HtmlMediaElement[F]] = F.delay(node.mediaElement)
  }
}

// def asyncSourceOpen(thunk: => Unit): F[Unit] = F.async_[Unit] { cb =>
//   src.addEventListener("sourceopen", _ => {
//     cb(Either.unit)
//   }, new dom.EventListenerOptions {
//     once = true
//   })
// }

// abstract class MediaSource[F[_]] {

//   def readyState: F[dom.MediaSourceReadyState]
//   // def addSourceBuffer(mimeType: String): Resource[F, SourceBuffer[F]]
//   // def endOfStream: F[Unit]
//   // def asyncSourceOpen(thunk: => Unit): F[Unit]

//   // def url: Resource[F, String]
// }k
