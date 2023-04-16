package integration.mapperonly

data class TargetB(
    override val id: Int,
    override val name: String,
    override val age: Int,
    override val address: String,
    override val zipcode: String = "",
    override val city: String?,
):ITarget
