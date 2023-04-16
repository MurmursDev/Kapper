package integration.multipletarget

import dev.murmurs.kapper.config.Mapper

@Mapper
interface TestMapper {
    fun map(source: Source): Target
}
