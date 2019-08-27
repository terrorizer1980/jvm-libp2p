package io.libp2p.core.mux.mplex

import io.libp2p.core.P2PAbstractChannel
import io.libp2p.core.P2PAbstractHandler
import io.libp2p.core.events.MuxSessionFailed
import io.libp2p.core.events.MuxSessionInitialized
import io.libp2p.core.mplex.MplexFrameCodec
import io.libp2p.core.multistream.Mode
import io.libp2p.core.multistream.ProtocolMatcher
import io.libp2p.core.mux.MuxHandler
import io.libp2p.core.mux.StreamMuxer
import io.libp2p.core.types.addLastX
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.concurrent.CompletableFuture

class MplexStreamMuxer : StreamMuxer {
    override val announce = "/mplex/6.7.0"
    override val matcher: ProtocolMatcher =
        ProtocolMatcher(Mode.STRICT, announce)
    var intermediateFrameHandler: ChannelHandler? = null

    override fun initializer(selectedProtocol: String): P2PAbstractHandler<StreamMuxer.Session> {
        return object : P2PAbstractHandler<StreamMuxer.Session> {
            override fun initChannel(ch: P2PAbstractChannel): CompletableFuture<StreamMuxer.Session> {
                val muxSessionFuture = CompletableFuture<StreamMuxer.Session>()
                ch.ch.pipeline().addLastX(MplexFrameCodec())
                intermediateFrameHandler?.also { ch.ch.pipeline().addLastX(it) }
                ch.ch.pipeline().addLast("MuxerSessionTracker", object : ChannelInboundHandlerAdapter() {
                    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
                        when (evt) {
                            is MuxSessionInitialized -> {
                                muxSessionFuture.complete(evt.session)
                                ctx.pipeline().remove(this)
                            }
                            is MuxSessionFailed -> {
                                muxSessionFuture.completeExceptionally(evt.exception)
                                ctx.pipeline().remove(this)
                            }
                            else -> super.userEventTriggered(ctx, evt)
                        }
                    }
                })
                ch.ch.pipeline().addBefore("MuxerSessionTracker", "MuxHandler", MuxHandler())
                return muxSessionFuture
            }
        }
    }
}