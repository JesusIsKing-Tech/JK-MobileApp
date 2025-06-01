package com.example.jkconect.model

data class EventoUser(
    val UsuarioId: Int?,
    val EventoId: Int?,
    var confirmado: Boolean?,
    var curtir: Boolean
    )

fun criarEventoUser(usuarioId: Int, eventoId: Int): EventoUser {
    return EventoUser(
        UsuarioId = usuarioId,
        EventoId = eventoId,
        confirmado = false,
        curtir = false
    )
}