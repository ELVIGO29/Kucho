package pe.kucho.app.data.model

// Paginación genérica de Spring Boot Data
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 20,
    val number: Int = 0
)
