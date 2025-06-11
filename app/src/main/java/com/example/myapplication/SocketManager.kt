
package com.example.myapplication

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {
    private const val SERVER_URL = "http://192.168.1.10:5000" // đổi theo IP máy server
    lateinit var socket: Socket

    fun initSocket() {
        try {
            val opts = IO.Options()
            socket = IO.socket(SERVER_URL, opts)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun connect() {
        socket.connect()
    }

    fun disconnect() {
        socket.disconnect()
    }
}
