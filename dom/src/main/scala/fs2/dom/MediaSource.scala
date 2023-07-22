package fs2.dom

import org.scalajs.dom
import scala.scalajs.js
import cats.effect._
import cats.syntax.all._

import scala.scalajs.js.annotation.*

object Url {

  private def createObjectUrl[F[_]](
      objectUrl: F[String]
  )(implicit F: Async[F]): Resource[F, Url] =
    Resource
      .make(objectUrl)(url => F.delay(dom.URL.revokeObjectURL(url)))
      .map(url => new dom.URL(url))

  def createObjectUrl[F[_]](blob: dom.Blob)(implicit F: Async[F]): Resource[F, Url] =
    createObjectUrl(F.delay(dom.URL.createObjectURL(blob)))

  def createObjectUrl[F[_]](src: MediaSource[F])(implicit F: Async[F]): Resource[F, Url] =
    createObjectUrl(F.delay(dom.URL.createObjectURL(src)))
}

import scala.scalajs.js.typedarray.ArrayBuffer

@JSGlobal
@js.native
class SourceBuffer[F[_]] protected () extends js.Any
object SourceBuffer {

  private[dom] implicit def fromJS[F[_]](buffer: dom.SourceBuffer): SourceBuffer[F] =
    buffer.asInstanceOf[SourceBuffer[F]]
  private[dom] implicit def toJS[F[_]](buffer: SourceBuffer[F]): dom.SourceBuffer =
    buffer.asInstanceOf[dom.SourceBuffer]

  implicit def ops[F[_]](buffer: SourceBuffer[F]): Ops[F] = new Ops(buffer)

  final class Ops[F[_]] private[SourceBuffer] (private val buffer: dom.SourceBuffer) {
    private def updateEnd(implicit F: Async[F]): F[Unit] =
      EventTargetHelpers.listenOnceAttemptDiscard[F, dom.SourceBuffer](
        buffer,
        "updateend",
        _.updating
      )
    def appendBuffer(src: ArrayBuffer)(implicit F: Async[F]): F[Unit] =
      F.delay(buffer.appendBuffer(src)) *> updateEnd
    def updating(implicit F: Async[F]): F[Boolean] = F.delay(buffer.updating)
    def abort(implicit F: Async[F]): F[Unit] = F.delay(buffer.abort())
    def remove(start: Double, end: Double)(implicit F: Async[F]): F[Unit] =
      F.delay(buffer.remove(start, end))
  }
}

import fs2._

@JSGlobal
@js.native
class MediaSource[F[_]] protected () extends js.Any

object MediaSource {

  def apply[F[_]]: MediaSource[F] = new dom.MediaSource()

  implicit def ops[F[_]](node: MediaSource[F]): Ops[F] = new Ops(node.asInstanceOf[dom.MediaSource])

  private[dom] implicit def toJS[F[_]](node: MediaSource[F]): dom.MediaSource =
    node.asInstanceOf[dom.MediaSource]
  private[dom] implicit def fromJS[F[_]](node: dom.MediaSource): MediaSource[F] =
    node.asInstanceOf[MediaSource[F]]

  final class Ops[F[_]] private[MediaSource] (private val src: dom.MediaSource) {
    def readyState(implicit F: Async[F]): F[dom.MediaSourceReadyState] = F.delay(src.readyState)
    def endOfStream(implicit F: Async[F]): F[Unit] = F.delay(src.endOfStream())
    def sourceOpen(implicit F: Async[F]): Stream[F, Event[F]] =
      EventTargetHelpers.listen[F, dom.Event](src, "sourceopen").map(Event(_))
    def addSourceBuffer(mimeType: String)(implicit F: Async[F]): F[SourceBuffer[F]] =
      F.delay(src.addSourceBuffer(mimeType))
  }
}

@JSGlobal
@js.native
class BaseAudioContext[F[_]] protected () extends js.Any

object BaseAudioContext {

  private[dom] implicit def toJS[F[_]](ctx: BaseAudioContext[F]): dom.BaseAudioContext =
    ctx.asInstanceOf[dom.BaseAudioContext]

  implicit def ops[F[_]](ctx: BaseAudioContext[F]): Ops[F] = new Ops(ctx)

  final class Ops[F[_]] private[BaseAudioContext] (private val ctx: dom.BaseAudioContext) {
    def sampleRate: Double = ctx.sampleRate
    def state(implicit F: Async[F]): F[String] = F.delay(ctx.state)
    def currentTime(implicit F: Async[F]): F[Double] = F.delay(ctx.currentTime)
    def destination: AudioDestinationNode[F] = ctx.destination
    def decodeAudioData(buf: ArrayBuffer)(implicit F: Async[F]): F[dom.AudioBuffer] =
      F.fromPromise(F.delay(ctx.decodeAudioData(buf)))

  }
}

@JSGlobal
@js.native
class AudioContext[F[_]] protected () extends BaseAudioContext[F]

object AudioContext {

  private[dom] implicit def fromJS[F[_]](ctx: dom.AudioContext): AudioContext[F] =
    ctx.asInstanceOf[AudioContext[F]]

  private[dom] implicit def toJS[F[_]](ctx: AudioContext[F]): dom.AudioContext =
    ctx.asInstanceOf[dom.AudioContext]

  def apply[F[_]]()(implicit F: Async[F]): Resource[F, AudioContext[F]] =
    Resource
      .make(F.delay(new dom.AudioContext()))(new Ops[F](_).close)
      .map(fromJS)

  def apply[F[_]](
      options: dom.AudioContextOptions
  )(implicit F: Async[F]): Resource[F, AudioContext[F]] =
    Resource
      .make(F.delay(new dom.AudioContext(options)))(new Ops[F](_).close)
      .map(fromJS)

  implicit def ops[F[_]](ctx: AudioContext[F]): Ops[F] = new Ops(ctx)

  final class Ops[F[_]] private[AudioContext] (private val ctx: dom.AudioContext) {
    def resume(implicit F: Async[F]): F[Unit] = F.fromPromise(F.delay(ctx.resume()))
    def suspend(implicit F: Async[F]): F[Unit] = F.fromPromise(F.delay(ctx.suspend()))
    def close(implicit F: Async[F]): F[Unit] = F.fromPromise(F.delay(ctx.close()))
  }
}

@JSGlobal
@js.native
class AudioNode[F[_]] protected () extends js.Any
object AudioNode {

  implicit def ops[F[_]](node: AudioNode[F]): Ops[F] = new Ops(node)

  private[dom] implicit def toJS[F[_]](ctx: AudioNode[F]): dom.AudioNode =
    ctx.asInstanceOf[dom.AudioNode]

  final class Ops[F[_]] private[AudioNode] (private val node: dom.AudioNode) extends AnyVal {
    def context(implicit F: Async[F]): F[AudioContext[F]] = F.delay(node.context)
    def connect(destination: AudioNode[F])(implicit F: Async[F]): F[Unit] =
      F.delay(node.connect(destination))
    def disconnect(destination: AudioNode[F])(implicit F: Async[F]): F[Unit] =
      F.delay(node.disconnect(destination))
    def numberOfInputs: Int = node.numberOfInputs
    def numberOfOutputs: Int = node.numberOfOutputs
    def channelCount: Int = node.channelCount
  }
}

@JSGlobal
@js.native
class AudioScheduledSourceNode[F[_]] protected () extends AudioNode[F] {}
object AudioScheduledSourceNode {

  private[dom] implicit def toJS[F[_]](
      node: AudioScheduledSourceNode[F]
  ): dom.AudioScheduledSourceNode = node.asInstanceOf[dom.AudioScheduledSourceNode]

  implicit def ops[F[_]](node: dom.AudioScheduledSourceNode): Ops[F] = new Ops(node)

  final class Ops[F[_]] private[AudioScheduledSourceNode] (
      private val node: dom.AudioScheduledSourceNode
  ) {
    def start(implicit F: Async[F]): F[Unit] = F.delay(node.start())
    def stop(implicit F: Async[F]): F[Unit] = F.delay(node.stop())
    def start(when: Double)(implicit F: Async[F]): F[Unit] = F.delay(node.start(when))
    def stop(when: Double)(implicit F: Async[F]): F[Unit] = F.delay(node.stop(when))
    def ended(implicit F: Async[F]): Stream[F, Event[F]] =
      EventTargetHelpers.listen[F, dom.Event](node, "ended").map(Event(_))
    def endedOnce(implicit F: Async[F]): F[Event[F]] =
      EventTargetHelpers.listenOnce[F, dom.Event](node, "ended").map(Event(_))
  }
}

@JSGlobal
@js.native
class MediaElementAudioSourceNode[F[_]] protected () extends AudioNode[F]

object MediaElementAudioSourceNode {

  def apply[F[_]](
      ctx: AudioContext[F],
      options: dom.MediaElementAudioSourceNodeOptions
  ): MediaElementAudioSourceNode[F] = new dom.MediaElementAudioSourceNode(ctx, options)

  private[dom] implicit def fromJS[F[_]](
      ctx: dom.MediaElementAudioSourceNode
  ): MediaElementAudioSourceNode[F] =
    ctx.asInstanceOf[MediaElementAudioSourceNode[F]]

  final class Ops[F[_]] private[MediaElementAudioSourceNode] (
      private val node: dom.MediaElementAudioSourceNode
  ) {
    def mediaElement: HtmlMediaElement[F] = node.mediaElement
  }
}

@JSGlobal
@js.native
class AudioDestinationNode[F[_]] protected () extends AudioNode[F]
object AudioDestinationNode {
  private[dom] implicit def fromJS[F[_]](node: dom.AudioDestinationNode): AudioDestinationNode[F] =
    node.asInstanceOf[AudioDestinationNode[F]]
}

@JSGlobal
@js.native
class AudioParam[F[_]] extends js.Any

object AudioParam {
  private[dom] implicit def fromJS[F[_]](param: dom.AudioParam): AudioParam[F] =
    param.asInstanceOf[AudioParam[F]]
  private[dom] implicit def toJS[F[_]](param: AudioParam[F]): dom.AudioParam =
    param.asInstanceOf[dom.AudioParam]

  implicit def ops[F[_]](param: AudioParam[F]): Ops[F] = new Ops(param)

  final class Ops[F[_]] private[AudioParam] (private val param: dom.AudioParam) {
    def value(implicit F: Async[F]): Ref[F, Double] =
      new WrappedRef[F, Double](() => param.value, param.value = _)(F)
    def setValueAtTime(value: Double, startTime: Double)(implicit F: Async[F]): F[Unit] =
      F.delay(param.setValueAtTime(value, startTime: Double))
    def linearRampToValueAtTime(value: Double, endTime: Double)(implicit F: Async[F]): F[Unit] =
      F.delay(param.linearRampToValueAtTime(value, endTime))
    def cancelScheduledValues(value: Double)(implicit F: Async[F]): F[Unit] =
      F.delay(param.cancelScheduledValues(value))

    def maxValue: Double = param.maxValue
    def minValue: Double = param.minValue
    def defaultValue: Double = param.defaultValue
  }

}

@JSGlobal
@js.native
class GainNode[F[_]] extends AudioNode[F]
object GainNode {

  implicit def ops[F[_]](node: GainNode[F]): Ops[F] = new Ops(node)

  def apply[F[_]](ctx: AudioContext[F]): GainNode[F] = new dom.GainNode(ctx)
  def apply[F[_]](ctx: AudioContext[F], options: dom.GainNodeOptions): GainNode[F] =
    new dom.GainNode(ctx, options)

  private[dom] implicit def fromJS[F[_]](node: dom.GainNode): GainNode[F] =
    node.asInstanceOf[GainNode[F]]

  private[dom] implicit def toJS[F[_]](ctx: GainNode[F]): dom.GainNode =
    ctx.asInstanceOf[dom.GainNode]

  final class Ops[F[_]] private[GainNode] (private val node: dom.GainNode) {
    def gain: AudioParam[F] = node.gain
  }
}

@JSGlobal
@js.native
class AudioBufferSourceNode[F[_]] protected () extends AudioScheduledSourceNode[F]
object AudioBufferSourceNode {

  def apply[F[_]](
      ctx: AudioContext[F],
      options: dom.AudioBufferSourceNodeOptions
  ): AudioBufferSourceNode[F] =
    new dom.AudioBufferSourceNode(ctx, options)

  private[dom] implicit def toJS[F[_]](node: AudioBufferSourceNode[F]): dom.AudioBufferSourceNode =
    node.asInstanceOf[dom.AudioBufferSourceNode]

  private[dom] implicit def fromJS[F[_]](
      node: dom.AudioBufferSourceNode
  ): AudioBufferSourceNode[F] = node.asInstanceOf[AudioBufferSourceNode[F]]

  implicit def ops[F[_]](node: AudioBufferSourceNode[F]): Ops[F] = new Ops(node)

  final class Ops[F[_]] private[AudioBufferSourceNode] (
      private val node: dom.AudioBufferSourceNode
  ) {
    def buffer(implicit F: Async[F]): Ref[F, Option[dom.AudioBuffer]] =
      new WrappedRef(() => Option(node.buffer), buffer => node.buffer = buffer.getOrElse(null))
    def loop(implicit F: Async[F]): Ref[F, Boolean] =
      new WrappedRef[F, Boolean](() => node.loop, node.loop = _)
    def playbackRate: AudioParam[F] = node.playbackRate

  }
}
