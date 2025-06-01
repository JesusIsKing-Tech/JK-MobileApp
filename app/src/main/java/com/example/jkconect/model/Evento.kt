import java.util.Date

data class Evento(
    val id: Int?,
    val titulo: String?,
    val data: Date?,
    val descricao: String?,
    val valor: Double?,
    val imagem: String?, // Alterado de ByteArray para String
    val imagemMimeType: String?,
    val horario: String = "12:00",
    val endereco: String = "rua das flores",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Evento

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}