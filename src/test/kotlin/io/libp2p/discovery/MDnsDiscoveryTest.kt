package io.libp2p.discovery

import io.libp2p.core.PeerId
import io.libp2p.core.PeerInfo
import io.libp2p.core.multiformats.Multiaddr
import io.libp2p.crypto.keys.generateEcdsaKeyPair
import io.libp2p.tools.NullHost
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class MDnsDiscoveryTest {
    val host = object: NullHost() {
        override val peerId: PeerId = PeerId.fromPubKey(
            generateEcdsaKeyPair().second
        )

        override fun listenAddresses(): List<Multiaddr> {
            return listOf(
                Multiaddr("/ip4/127.0.0.1/tcp/4000"),
                Multiaddr("/ip4/10.2.7.1/tcp/9999")
            )
        }
    }

    @Test
    fun `start and stop discovery`() {
        val discoverer = MDnsDiscovery(host)

        discoverer.start()
        TimeUnit.SECONDS.sleep(2)
        discoverer.stop()
    }

    @Test
    fun `start discovery and listen for self`() {
        var peerInfo: PeerInfo? = null
        val discoverer = MDnsDiscovery(host)

        discoverer.onPeerFound {
            discoverer.stop()
            peerInfo = it;
        }

        discoverer.start()
        (1..120).forEach {
            TimeUnit.SECONDS.sleep(1)
            print('.')
        }
        println()


        assertEquals(host.peerId, peerInfo?.peerId)
        assertEquals(host.listenAddresses().size, peerInfo?.addresses?.size)
    }
}