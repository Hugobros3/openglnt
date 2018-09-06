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