import org.joml.Vector2d
import org.joml.Vector4d
import javax.imageio.ImageIO

class Shader<VF : Vertex, ID : IntermediaryData>(
        val vertexShader: ((VertexShaderCtx<VF>) -> ID),
        val fragmentShader: (FragmentShaderCtx<ID>) -> Unit
) {
    interface VertexShaderCtx<VF : Vertex> {
        val vertex: VF
    }

    interface FragmentShaderCtx<ID : IntermediaryData> {
        val frameNumber: Int

        val passedData: ID
        var output: Vector4d
    }
}

open class IntermediaryData(var position: Vector4d) : Cloneable {
    public override fun clone() = super.clone()
}

object DemoShaders {
    val checker = Texture(ImageIO.read(javaClass.getResource("/pepe.png")))

    /*val basicShader = Shader<Vertex, IntermediaryDataWithTexcoord>({
        IntermediaryDataWithTexcoord(it.vertex.position, it.vertex.textureCoordinate)
    }, {
        //it.output = Vector4d(it.passedData.texcoord.x, it.passedData.texcoord.y, 0.0, 1.0)
        it.output = checker.sample(it.passedData.texcoord.mul(1.0 + 0.5 * Math.sin(it.frameNumber * 0.1)))
    })*/

}