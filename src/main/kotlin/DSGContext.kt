import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.util.concurrent.Semaphore
import javax.swing.JFrame
import javax.swing.JPanel

class DSGContext {
    var frame = 0

    lateinit var jframe: JFrame

    val image = BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB)
    val zbuffer = FloatArray(512 * 512)
    val graphics = Graphics(this)

    var lastframe = -1L
    var fps = 0.0

    val semaphore = Semaphore(0)
    val panel = object : JPanel() {

        override fun paint(g: java.awt.Graphics?) {
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

    fun openWindow() {
        jframe = JFrame("DamnSimpleGraphics")

        jframe.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        jframe.contentPane = panel
        jframe.contentPane.size.setSize(512, 512)
        jframe.setSize(512, 512)
        //jframe.pack()

        jframe.isVisible = true
    }

    fun finishFrame() {
        jframe.repaint()
        Thread.sleep(10)
        frame++

        val delta = System.currentTimeMillis() - lastframe
        fps = 1.0 / (delta / 1000.0)
        lastframe = System.currentTimeMillis()

        jframe.title = "fps = $fps"
    }
}