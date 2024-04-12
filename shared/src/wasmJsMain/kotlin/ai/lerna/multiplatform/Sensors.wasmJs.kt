package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.utils.DateUtil
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get

actual class Sensors actual constructor(
	_context: KMMContext,
	_modelData: ModelData
) : SensorInterface {

	private var context = _context
	private var modelData = _modelData
	private var _isEnabled = false
	private var keyPressedCount = 0
	private var clickCount = 0

	actual override val data: CommonFlow<SensorDataInterface?>
		get() = TODO("Not yet implemented")

	actual override val isEnabled: Boolean
		get() = _isEnabled

	actual override fun start() {
		_isEnabled = true

		document.body?.onmousemove = this::mouseMoveListener
		document.body?.onclick = this::clickListener
		document.body?.onkeyup = this::keyUpListener
		document.body?.addEventListener("touchstart", this::touchListener)
	}

	actual override fun stop() {
		_isEnabled = false
		document.body?.onmousemove = null
		document.body?.onclick = null
		document.body?.onkeyup = null
		document.body?.removeEventListener("touchstart", this::touchListener)
	}

	actual override fun updateData() {
		modelData.setScreenSize(window.screen.width + 0.0f, window.screen.height + 0.0f)
		modelData.setOrientation(if (window.screen.width > window.screen.height) 1f else 0f)
		modelData.setKeyPressedCounter(keyPressedCount + 0.0f)
		keyPressedCount = 0
		modelData.setClickCounter(clickCount + 0.0f)
		clickCount = 0
		modelData.setHistory(DateUtil().now())
	}

	private fun mouseMoveListener(event: MouseEvent) {
		modelData.setMousePosition(event.x.toFloat(), event.y.toFloat())
	}

	private fun clickListener(event: MouseEvent) {
		modelData.setClickPosition(event.x.toFloat(), event.y.toFloat())
		clickCount++
	}

	private fun keyUpListener(event: KeyboardEvent) {
		keyPressedCount++
	}

	private fun touchListener(event: Event) {
		event as TouchEvent
		modelData.setClickPosition(event.touches[0]?.pageX?.toFloat() ?: 0f, event.touches[0]?.pageY?.toFloat() ?: 0f)
	}
}