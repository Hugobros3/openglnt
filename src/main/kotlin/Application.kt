import org.joml.Vector2d
import org.joml.Vector4d
import javax.imageio.ImageIO

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        val context = DSGContext()
        context.openWindow()

        val graphics = context.graphics

        val grey = Vector4d(0.5, 0.5, 0.5, 1.0)
        val green = Vector4d(0.0, 1.0, 0.0, 1.0)
        val red = Vector4d(1.0, 0.0, 0.0, 1.0)

        var a = VertexWithTexcoord(Vector4d(0.5, 0.2, 0.0, 1.0))
        a.textureCoordinate = Vector2d(0.5, 0.0)

        var b = VertexWithTexcoord(Vector4d(0.2, 0.8, 0.0, 1.0))
        b.textureCoordinate = Vector2d(0.0, 1.0)

        var c = VertexWithTexcoord(Vector4d(0.8, 0.8, 0.0, 1.0))
        c.textureCoordinate = Vector2d(1.0, 1.0)

        val vertexShader: Shader.VertexShaderCtx<VertexWithTexcoord>.() -> IntermediaryDataWithTexcoord = {
            //Just return the intermediate data structure without modifications
            IntermediaryDataWithTexcoord(vertex.position, vertex.textureCoordinate)
        }

        val texture = Texture(ImageIO.read(javaClass.getResource("/pepe.png")))
        val fragmentShader: Shader.FragmentShaderCtx<IntermediaryDataWithTexcoord>.() -> Unit = {
            output = texture.sample(passedData.texcoord.mul(1.0 + 0.5 * Math.sin(frameNumber * 0.1)))
        }

        graphics.usingShader(Shader(vertexShader, fragmentShader))

        while (true) {
            graphics.clear(grey)
            //graphics.line(Vector2d(0.0, 0.0), Vector2d(1.0, 1.0 + Math.sin(t)), green)

            graphics.triangle(a, b, c, red)
            context.finishFrame()
        }
    }

    class VertexWithTexcoord(position: Vector4d) : Vertex(position) {
        var textureCoordinate = Vector2d(0.0)
    }

    class IntermediaryDataWithTexcoord(position: Vector4d, var texcoord: Vector2d) : IntermediaryData(position)
}