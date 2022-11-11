package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.MpcResponse
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.utils.io.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlin.system.*

class MpcService(host: String) {
	private val mpcHost = host

	fun lerna(data: String, size: Long): MpcResponse {
		CoroutineScope(Dispatchers.Default).launch {
			val selectorManager = SelectorManager(Dispatchers.Default)
			//val config = TLSConfigBuilder.add
			val socket = aSocket(selectorManager).tcp().connect(mpcHost, 31337).tls(Dispatchers.Default) {
				TLSConfigBuilder().build()
			}
			val receiveChannel = socket.openReadChannel()
			val sendChannel = socket.openWriteChannel(autoFlush = true)

			launch(Dispatchers.Default) {
				while (true) {
					val greeting = receiveChannel.readUTF8Line()
					if (greeting != null) {
						println(greeting)
						sendChannel.writeStringUtf8("OK\n")
					} else {
						println("Server closed a connection")
						socket.close()
						selectorManager.close()
						//exitProcess(0)
					}
				}
			}

			sendChannel.writeStringUtf8("$data\n")
		}
		return MpcResponse()
	}

//	fun lerna(data: String, size: Long): MpcResponse {
//		val factory = SSLSocketFactory.getDefault()
//		val socket = factory.createSocket(mpcHost, 31337) as SSLSocket
//		val out = PrintWriter(socket.outputStream, true)
//		Log.d("LernaMPC", "MPC Request: $data")
//		out.print(data)
//		out.flush()
//		if (out.checkError()) {
//			Log.e("LernaMPC", "SSLSocketClient error")
//		}
//
//		/* read response */
//		val input = BufferedReader(InputStreamReader(socket.inputStream))
//		val message = input.readLine()
//
//		out.print("OK")
//		out.flush()
//		out.print(size.toString())
//		out.flush()
//
//		input.close()
//		out.close()
//		socket.close()
//
//		// MPC Response: <Body><CompID>72338</CompID><Share>-47518.000000</Share><Epsilon>1000.0</Epsilon></Body>
//		Log.d("LernaMPC", "MPC Response: $message")
//		return jsonMapper.readValue(xmlToJson(message.toString()), MpcResponse::class.java)
//	}

	/**
	 * Helper method to replace MPC response xml to json. Need to improved if the contract was changed
	 */
	fun xmlToJson(xml: String): String {
		return xml
			.replace("<Body>", "{")
			.replace("</Body>", "}")
			.replace("</CompID>", ",")
			.replace("</Share>", ",")
			.replace("</Epsilon>", "")
			.replace("<", "\"")
			.replace(">", "\":")
	}

//		public fun TLSConfigBuilder.addKeyStore(store: KeyStore, password: CharArray, alias: String? = null) {
//			addKeyStore(store, password as CharArray?, alias)
//		}
}