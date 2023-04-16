package integration.mappingconfig

import dev.murmurs.kapper.config.Mapper
import dev.murmurs.kapper.config.Mapping


@Mapper
interface TestMapper {
    @Mapping(target = "zipcode", ignore = true)
    @Mapping(target = "age", defaultValue = "100")
    @Mapping(target = "city", source = "name")
    fun map(source: Source): Target
}
