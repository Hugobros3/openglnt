import org.joml.Vector2d
import org.joml.Vector4d
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.util.concurrent.Semaphore
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel

object Application {

    var frame = 0

    lateinit var jframe: JFrame

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
    }

    @JvmStatic
    fun main(args: Array<String>) {
        openWindow()

        val grey = Vector4d(0.5, 0.5, 0.5, 1.0)
        val green = Vector4d(0.0, 1.0, 0.0, 1.0)
        val red = Vector4d(1.0, 0.0, 0.0, 1.0)

        var a = VertexWithTexcoord(Vector4d(0.5, 0.2, 0.0, 1.0))
        a.textureCoordinate = Vector2d(0.5, 0.0)

        var b = VertexWithTexcoord(Vector4d(0.2, 0.8, 0.0, 1.0))
        b.textureCoordinate = Vector2d(0.0, 1.0)

        var c = VertexWithTexcoord(Vector4d(0.8, 0.8, 0.0, 1.0))
        c.textureCoordinate = Vector2d(1.0, 1.0)

        val vertexShader : Shader.VertexShaderCtx<VertexWithTexcoord>.() -> IntermediaryDataWithTexcoord = {
            //Just return the intermediate data structure without modifications
            IntermediaryDataWithTexcoord(vertex.position, vertex.textureCoordinate)
        }

        val texture = Texture(ImageIO.read(javaClass.getResource("/pepe.png")))
        val fragmentShader : Shader.FragmentShaderCtx<IntermediaryDataWithTexcoord>.() -> Unit = {
            output = texture.sample(passedData.texcoord.mul(1.0 + 0.5 * Math.sin(frameNumber * 0.1)))
        }

        graphics.usingShader(Shader(vertexShader, fragmentShader))

        while (true) {
            graphics.clear(grey)
            //graphics.line(Vector2d(0.0, 0.0), Vector2d(1.0, 1.0 + Math.sin(t)), green)
            graphics.triangle(a, b, c, red)
            finishFrame()
        }
    }

    class VertexWithTexcoord(position: Vector4d) : Vertex(position)  {
        var textureCoordinate = Vector2d(0.0)
    }

    class IntermediaryDataWithTexcoord(position: Vector4d, var texcoord: Vector2d) : IntermediaryData(position)
}