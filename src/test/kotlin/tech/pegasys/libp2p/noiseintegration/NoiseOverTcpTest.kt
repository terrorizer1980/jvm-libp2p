package tech.pegasys.libp2p.noiseintegration

import io.libp2p.core.multiformats.Multiaddr
import io.libp2p.core.transport.ConnectionUpgrader
import io.libp2p.core.transport.tcp.TcpTransport
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class NoiseOverTcpTest {

    companion object {
        @JvmStatic
        fun validMultiaddrs() = listOf(
            "/ip4/1.2.3.4/tcp/1234",
            "/ip6/fe80::6f77:b303:aa6e:a16/tcp/42"
        ).map { Multiaddr(it) }

        @JvmStatic
        fun invalidMultiaddrs() = listOf(
            "/ip4/1.2.3.4/udp/42",
            "/unix/a/file/named/tcp"
        ).map { Multiaddr(it) }
    }

    private val upgrader = ConnectionUpgrader(emptyList(), emptyList())

    @ParameterizedTest
    @MethodSource("validMultiaddrs")
    fun NoiseOverTcpTestValidAddrs(addr: Multiaddr) {
        val tcp = NoiseOverTcp()
        assert(tcp.setupTcpTransport(addr))
    }

    @ParameterizedTest
    @MethodSource("invalidMultiaddrs")
    fun `handles(addr) returns false if addr does not contain tcp protocol`(addr: Multiaddr) {
        val tcp = TcpTransport(upgrader)
        assert(!tcp.handles(addr))
    }
}