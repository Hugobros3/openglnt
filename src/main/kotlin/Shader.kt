import org.joml.Vector2d
import org.joml.Vector4d
import javax.imageio.ImageIO

class Shader<VF : Vertex, ID : IntermediaryData> {
    var vertexShader: ((VertexShaderCtx<VF, ID>) -> Unit) = {
        it.output.position = it.vertex.position
    }
    var fragmentShader: (FramgnentShaderCtx<ID>) -> Unit = {
        it.output = Vector4d(1.0, 1.0, 1.0, 1.0)
    }

    interface VertexShaderCtx<VF: Vertex, ID: IntermediaryData> {
        val vertex: VF
        val output: ID
    }

    interface FramgnentShaderCtx<ID: IntermediaryData> {
        val passedData: ID
        var output: Vector4d
    }
}

open class IntermediaryData(var position: Vector4d) {

}

object DemoShaders {
    val basicShader = Shader<Vertex, IntermediaryDataWithTexcoord>()

    val checker = Texture(ImageIO.read(javaClass.getResource("/pepe.png")))

    init {
        basicShader.fragmentShader = {

        }
    }

    class IntermediaryDataWithTexcoord(position: Vector4d, var texcoord: Vector2d) : IntermediaryData(position)
}