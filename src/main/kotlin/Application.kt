import org.joml.Vector2d
import org.joml.Vector4d
import org.joml.Vector4dc
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.util.concurrent.Semaphore
import javax.swing.JFrame
import javax.swing.JPanel

object Application {

    lateinit var frame: JFrame

    val image = BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB)
    val graphics = Graphics(image)

    val semaphore = Semaphore(0)
    val panel = object : JPanel() {

        override fun paint(g: Graphics?) {
            super.paint(g)

            val observer = object : ImageObserver {
                override fun imageUpdate(img: Image?, infoflags: Int, x: Int, y: Int, width: Int, height: Int): Boolean {
                    //semaphore.release()
                    return true
                }

            }
            g!!.drawImage(image, 0, 0, 512, 512, Color.gray, observer)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {

        openWindow()

        val grey = Vector4d(0.5, 0.5, 0.5, 1.0)
        val green = Vector4d(0.0, 1.0, 0.0, 1.0)
        val red = Vector4d(1.0, 0.0, 0.0, 1.0)

        var a = Vertex(Vector4d(0.5, 0.2, 0.0, 1.0))
        a.textureCoordinate = Vector2d(0.5, 0.0)

        var b = Vertex(Vector4d(0.2, 0.8, 0.0, 1.0))
        b.textureCoordinate = Vector2d(0.0, 1.0)

        var c = Vertex(Vector4d(0.8, 0.8, 0.0, 1.0))
        c.textureCoordinate = Vector2d(1.0, 1.0)

        var f = 0
        var t = 0.0
        while (true) {
            graphics.clear(grey)
            graphics.line(Vector2d(0.0, 0.0), Vector2d(1.0, 1.0 + Math.sin(t)), green)

            /*if(f % 100 == 0) {
                a = Vector2d(Math.random(), Math.random())
                b = Vector2d(Math.random(), Math.random())
                c = Vector2d(Math.random(), Math.random())
            }*/

            graphics.triangle(a, b, c, red)
            t += 0.1
            f++

            frame.repaint()
            Thread.sleep(10)
        }
    }

    fun openWindow() {
        frame = JFrame("DamnSimpleGraphics")

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        frame.contentPane = panel
        frame.contentPane.size.setSize(512, 512)
        frame.setSize(512, 512)
        //frame.pack()

        frame.isVisible = true
    }
}