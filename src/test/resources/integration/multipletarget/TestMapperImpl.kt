package integration.multipletarget

public class TestMapperImpl : TestMapper {
    public override fun map(source: Source): Target {
        val id = source.id
        val name = source.name
        val age = source.age
        val address = if (source.address == null) {
            throw IllegalArgumentException("source.address is null")
        } else {
            source.address
        }
        return Target(id = id, name = name, age = age, address = address, city = null)
    }
}
