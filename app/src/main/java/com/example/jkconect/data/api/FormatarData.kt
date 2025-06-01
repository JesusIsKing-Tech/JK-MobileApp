package com.example.jkconect.data.api

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatarData(data: Date): String {
    val formatador = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return formatador.format(data)
}