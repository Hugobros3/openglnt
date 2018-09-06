import org.joml.Matrix4d
import org.joml.Vector2d
import org.joml.Vector4d
import javax.imageio.ImageIO

object CubeApplication {

    @JvmStatic
    fun main(args: Array<String>) {
        val context = DSGContext()
        context.openWindow()

        val graphics = context.graphics

        val grey = Vector4d(0.5, 0.5, 0.5, 1.0)
        val green = Vector4d(0.0, 1.0, 0.0, 1.0)
        val red = Vector4d(1.0, 0.0, 0.0, 1.0)

        /** FRONT */
        var aFront = VertexWithTexcoord(Vector4d(-1.0, -1.0, -1.0, 1.0))
        aFront.textureCoordinate = Vector2d(0.0, 1.0)
        var bFront = VertexWithTexcoord(Vector4d(-1.0, 1.0, -1.0, 1.0))
        bFront.textureCoordinate = Vector2d(0.0, 0.0)
        var cFront = VertexWithTexcoord(Vector4d(1.0, 1.0, -1.0, 1.0))
        cFront.textureCoordinate = Vector2d(1.0, 0.0)
        var dFront = VertexWithTexcoord(Vector4d(1.0, -1.0, -1.0, 1.0))
        dFront.textureCoordinate = Vector2d(1.0, 1.0)

        /** BACK */
        var aBack = VertexWithTexcoord(Vector4d(-1.0, -1.0, 1.0, 1.0))
        aBack.textureCoordinate = Vector2d(0.0, 1.0)
        var bBack = VertexWithTexcoord(Vector4d(-1.0, 1.0, 1.0, 1.0))
        bBack.textureCoordinate = Vector2d(0.0, 0.0)
        var cBack = VertexWithTexcoord(Vector4d(1.0, 1.0, 1.0, 1.0))
        cBack.textureCoordinate = Vector2d(1.0, 0.0)
        var dBack = VertexWithTexcoord(Vector4d(1.0, -1.0, 1.0, 1.0))
        dBack.textureCoordinate = Vector2d(1.0, 1.0)

        /** TOP */
        var bTopFront = VertexWithTexcoord(Vector4d(-1.0, 1.0, -1.0, 1.0))
        bTopFront.textureCoordinate = Vector2d(0.0, 1.0)
        var cTopFront = VertexWithTexcoord(Vector4d(1.0, 1.0, -1.0, 1.0))
        cTopFront.textureCoordinate = Vector2d(1.0, 1.0)
        var bTopBack = VertexWithTexcoord(Vector4d(-1.0, 1.0, 1.0, 1.0))
        bTopBack.textureCoordinate = Vector2d(0.0, 0.0)
        var cTopBack = VertexWithTexcoord(Vector4d(1.0, 1.0, 1.0, 1.0))
        cTopBack.textureCoordinate = Vector2d(1.0, 0.0)

        /** BOTTOM */
        var aBotFront = VertexWithTexcoord(Vector4d(-1.0, -1.0, -1.0, 1.0))
        aBotFront.textureCoordinate = Vector2d(0.0, 1.0)
        var dBotFront = VertexWithTexcoord(Vector4d(1.0, -1.0, -1.0, 1.0))
        dBotFront.textureCoordinate = Vector2d(1.0, 1.0)
        var aBotBack = VertexWithTexcoord(Vector4d(-1.0, -1.0, 1.0, 1.0))
        aBotBack.textureCoordinate = Vector2d(0.0, 0.0)
        var dBotBack = VertexWithTexcoord(Vector4d(1.0, -1.0, 1.0, 1.0))
        dBotBack.textureCoordinate = Vector2d(1.0, 0.0)

        /** LEFT */
        var aLeftFront = VertexWithTexcoord(Vector4d(-1.0, -1.0, -1.0, 1.0))
        aLeftFront.textureCoordinate = Vector2d(0.0, 1.0)
        var bLeftFront = VertexWithTexcoord(Vector4d(-1.0, 1.0, -1.0, 1.0))
        bLeftFront.textureCoordinate = Vector2d(0.0, 0.0)
        var aLeftBack = VertexWithTexcoord(Vector4d(-1.0, -1.0, 1.0, 1.0))
        aLeftBack.textureCoordinate = Vector2d(1.0, 1.0)
        var bLeftBack = VertexWithTexcoord(Vector4d(-1.0, 1.0, 1.0, 1.0))
        bLeftBack.textureCoordinate = Vector2d(1.0, 0.0)

        /** RIGHT */
        var cRightFront = VertexWithTexcoord(Vector4d(1.0, 1.0, -1.0, 1.0))
        cRightFront.textureCoordinate = Vector2d(0.0, 0.0)
        var dRightFront = VertexWithTexcoord(Vector4d(1.0, -1.0, -1.0, 1.0))
        dRightFront.textureCoordinate = Vector2d(0.0, 1.0)
        var cRightBack = VertexWithTexcoord(Vector4d(1.0, 1.0, 1.0, 1.0))
        cRightBack.textureCoordinate = Vector2d(1.0, 0.0)
        var dRightBack = VertexWithTexcoord(Vector4d(1.0, -1.0, 1.0, 1.0))
        dRightBack.textureCoordinate = Vector2d(1.0, 1.0)

        val matrix = Matrix4d()

        val vertexShader: Shader.VertexShaderCtx<VertexWithTexcoord>.() -> IntermediaryDataWithTexcoord = {
            val transformed = Vector4d()
            matrix.transform(vertex.position, transformed)

            IntermediaryDataWithTexcoord(transformed, vertex.textureCoordinate)
        }

        val texture = Texture(ImageIO.read(javaClass.getResource("/pepe.png")))
        val fragmentShader: Shader.FragmentShaderCtx<IntermediaryDataWithTexcoord>.() -> Unit = {
            output = texture.sample(passedData.texcoord.mul(1.0 + 0.0 * Math.sin(frameNumber * 0.1)))
            /*output.x = -passedData.position.z
            output.y = -passedData.position.z
            output.z = -passedData.position.z*/
        }

        graphics.usingShader(Shader(vertexShader, fragmentShader))

        while (true) {
            matrix.identity()
            matrix.translate(0.5, 0.5, 0.0)
            matrix.scale(0.5, 0.5, 0.5)

            matrix.perspective(Math.PI / 2, 1.0, 0.1, 100.0)

            matrix.translate(0.0, 0.0, 4.0)
            matrix.rotate(context.frame * 0.01, 1.0, 0.0, 0.0)
            matrix.rotate(context.frame * 0.1, 0.0, 1.0, 0.0)

            graphics.clear(grey)
            //graphics.line(Vector2d(0.0, 0.0), Vector2d(1.0, 1.0 + Math.sin(t)), green)

            /** FRONT */
            graphics.triangle(aFront, bFront, cFront, red)
            graphics.triangle(aFront, dFront, cFront, red)

            /** BACK */
            graphics.triangle(aBack, bBack, cBack, red)
            graphics.triangle(aBack, dBack, cBack, red)

            /** LEFT */
            graphics.triangle(aLeftFront, aLeftBack, bLeftBack, red)
            graphics.triangle(aLeftFront, bLeftFront, bLeftBack, red)

            /** RIGHT */
            graphics.triangle(cRightFront, cRightBack, dRightBack, red)
            graphics.triangle(cRightFront, dRightFront, dRightBack, red)

            /** BOTTOM */
            graphics.triangle(aBotFront, aBotBack, dBotBack, red)
            graphics.triangle(aBotFront, dBotFront, dBotBack, red)

            /** TOP */
            graphics.triangle(bTopFront, bTopBack, cTopBack, red)
            graphics.triangle(bTopFront, cTopFront, cTopBack, red)

            context.finishFrame()
        }
    }

    class VertexWithTexcoord(position: Vector4d) : Vertex(position) {
        var textureCoordinate = Vector2d(0.0)
    }

    class IntermediaryDataWithTexcoord(position: Vector4d, var texcoord: Vector2d) : IntermediaryData(position)
}