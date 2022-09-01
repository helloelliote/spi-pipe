package kr.djspi.pipe01

import android.content.Context
import com.novoda.merlin.*

object MerlinInstance : Connectable, Disconnectable, Bindable {

    private lateinit var merlin: Merlin
    var isConnected: Boolean = true

    fun initiateNetworkMonitor(context: Context) {
        merlin = Merlin.Builder()
            .withConnectableCallbacks()
            .withDisconnectableCallbacks()
            .withBindableCallbacks()
            .build(context)
    }

    fun registerNetworkCallback() {
        merlin.apply {
            registerConnectable(this@MerlinInstance)
            registerDisconnectable(this@MerlinInstance)
            registerBindable(this@MerlinInstance)
        }
    }

    override fun onConnect() {
        isConnected = true
    }

    override fun onDisconnect() {
        isConnected = false
    }

    override fun onBind(networkStatus: NetworkStatus?) {
        networkStatus?.let {
            if (!it.isAvailable) onDisconnect()
        }
    }

    fun bind() = merlin.bind()

    fun unbind() = merlin.unbind()
}
