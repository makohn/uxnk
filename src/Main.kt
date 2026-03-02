import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import util.*
import varvara.Varvara
import varvara.device.ConsoleDevice
import java.io.File
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

sealed interface Event {
    class ButtonPressed(val button: UByte): Event
    class ButtonReleased(val button: UByte): Event
    class KeyTyped(val key: Int): Event
    class MouseMoved(val x: Int, val y: Int): Event
    class MouseScrolled(val x: Int, val y: Int): Event
    class MousePressed(val button: UByte): Event
    class MouseReleased(val button: UByte): Event
    class StdIn(val c: Char): Event
    class AudioFinished(val id: Int): Event
    object Repaint: Event
    object Resize: Event
}

val events = Channel<Event>()

suspend fun main(args: Array<String>) {
    val romFile = args[0]
    val varvara = Varvara()
    val uxn = varvara.uxn
    val rom = File(romFile).readBytes().toUByteArray()
    uxn.loadRom(rom)
    uxn.eval()

    if (varvara.console.vector != UShort_0) {
        for (i in 1..<args.size) {
            val arg = args[i]
            for (c in arg) {
                varvara.console.input(c, ConsoleDevice.ARGUMENT)
            }
            varvara.console.input(
                '\n',
                if (i == args.lastIndex) ConsoleDevice.ARGUMENT_END else ConsoleDevice.ARGUMENT_SPACER
            )
        }
    }

    AudioPlayer.start(varvara)
    val gui = Gui(varvara)
    SwingUtilities.invokeLater { gui.start() }

    CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            val c = readln()[0]
            events.send(Event.StdIn(c))
        }
    }

    for (event in events) {
        when (event) {
            is Event.ButtonPressed -> {
                val controller = varvara.controller
                controller.setButton(event.button)
                uxn.eval(controller.vector)
                controller.setKey(0)
            }
            is Event.ButtonReleased -> {
                val controller = varvara.controller
                controller.unsetButton(event.button)
                uxn.eval(controller.vector)
                controller.setKey(0)
            }
            is Event.KeyTyped -> {
                val controller = varvara.controller
                controller.setKey(event.key)
                uxn.eval(controller.vector)
                controller.setKey(0)
            }
            is Event.MouseMoved -> {
                val mouse = varvara.mouse
                mouse.setPos(event.x, event.y)
                if (mouse.vector != UShort_0) uxn.eval(mouse.vector)
            }
            is Event.MouseScrolled -> {
                val mouse = varvara.mouse
                mouse.setScroll(event.x, event.y)
                uxn.eval(mouse.vector)
                mouse.setScroll(0, 0)
            }
            is Event.MousePressed -> {
                val mouse = varvara.mouse
                mouse.setButton(event.button)
                uxn.eval(mouse.vector)
            }
            is Event.MouseReleased -> {
                val mouse = varvara.mouse
                mouse.unsetButton(event.button)
                uxn.eval(mouse.vector)
            }
            is Event.Repaint -> {
                val screen = varvara.screen
                if (screen.vector != UShort_0) uxn.eval(screen.vector)
                SwingUtilities.invokeLater { gui.redraw() }
            }
            is Event.StdIn -> {
                val console = varvara.console
                console.input(event.c, ConsoleDevice.STDIN)
            }
            is Event.AudioFinished -> {
                val audio = varvara.audio[event.id]
                if (audio.vector != UShort_0) varvara.uxn.eval(audio.vector)
            }
            is Event.Resize -> {
                gui.resize()
            }
        }
        val state = varvara.system.state
        if (state != UByte_0) {
            exitProcess(state.toInt() and 0x7f)
        }
    }
}