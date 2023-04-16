package integration.mapperonly

data class TargetA(
    override val id: Int,
    override val name: String,
    override val age: Int,
    override val address: String,
    override val zipcode: String = "",
    override val city: String?,
):ITarget
