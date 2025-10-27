import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

data class Version(
    val major: String,
    val minor: String,
    val revision: String,
    val classifier: String?,
) : Serializable {

    override fun toString(): String = buildString {
        append("$major.$minor.$revision")
        if (classifier?.isNotBlank() == true) append("-$classifier")
    }

    private fun writeObject(out: ObjectOutputStream) {
        out.defaultWriteObject()
    }

    private fun readObject(out: ObjectInputStream) {
        out.defaultReadObject()
    }
}
