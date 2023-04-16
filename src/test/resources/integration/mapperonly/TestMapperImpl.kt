package integration.mapperonly

public class TestMapperImpl : TestMapper {
    public override fun map(source: Source): ITarget {
        val id = source.id
        val name = source.name.toString()
        val age = source.age
        val address = if (source.address == null) {
            throw IllegalArgumentException("source.address is null")
        } else {
            source.address
        }
        return when (source.name.toString()) {
            "TargetA" -> TargetA(id = id, name = name, age = age, address = address, city = null)
            "TargetB" -> TargetB(id = id, name = name, age = age, address = address, city = null)
            else -> throw IllegalArgumentException("Unknown discriminator value")
        }
    }
}
