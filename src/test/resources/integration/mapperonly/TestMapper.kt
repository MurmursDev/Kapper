package integration.mapperonly

import dev.murmurs.kapper.config.Mapper
import dev.murmurs.kapper.config.MultipleTarget
import dev.murmurs.kapper.config.TargetType


@Mapper
interface TestMapper {
    @MultipleTarget(
        "name",
        targetTypes = [
            TargetType("TargetA", TargetA::class),
            TargetType("TargetB", TargetB::class),
        ]
    )
    fun map(source: Source): ITarget
}
