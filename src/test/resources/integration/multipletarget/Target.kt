package integration.multipletarget

data class Target(
    val id: Int,
    val name: String,
    val age: Int,
    val address: String,
    val zipcode: String = "",
    val city: String?,
)
