import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomDateTypeAdapter : TypeAdapter<Date>() {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(dateFormat.format(value))
        }
    }

    override fun read(reader: JsonReader): Date? {
        return try {
            val dateStr = reader.nextString()
//            Log.d("CustomDateTypeAdapter", "Lendo data: $dateStr")
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            Log.e("CustomDateTypeAdapter", "Erro ao parsear data", e)
            null
        }
    }
}