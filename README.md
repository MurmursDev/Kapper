![Maven Central](https://img.shields.io/maven-central/v/dev.murmurs.kapper/kapper?color=brightgreen)
![GitHub](https://img.shields.io/github/license/murmursdev/kapper?color=brightgreen)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg)](https://github.com/murmursdev/kapper/issues)

# Kapper

Kapper is a code generator for Kotlin that simplifies mapping between data classes.

It was designed to map one class to an interface which has multiple implementations.

## Usage

### Installation

Add the following to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("dev.murmurs.kapper:kapper:${latest}")
}
```

### Example

Given the following data classes:

```kotlin

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val address: Address,
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
)

```

Kapper can generate a mapper that converts a `User` to a `UserDto`:

```kotlin
@Mapper
interface UserMapper {
    fun userToDto(user: User): UserDto
}
```

The generated mapper will be named `UserMapperImpl` and will be in the same package as the mapper interface.

#### Multiple Targets

In ActivityPub, Activity has multiple implementations like Create, Update, Delete, etc. But the properties are the same
for all of them.

When mapping from openapi generated single ActivityView classes to Multiple Activity implementations, we can use Kapper
to map the properties to an interface:

```kotlin
data class ActivityView(
    val id: String,
    val type: String,
    val actor: String,
    val `object`: String,
)

interface Activity {
    val id: String
    val type: String
    val actor: String
    val `object`: String
}

data class Create(
    override val id: String,
    override val type: String,
    override val actor: String,
    override val `object`: String,
) : Activity

data class Update(
    override val id: String,
    override val type: String,
    override val actor: String,
    override val `object`: String,
) : Activity

@Mapper
interface ActivityMapper {
    @MultipleTarget(
        "type",
        targetTypes = [
            TargetType(
                discriminatorValue = "Create",
                targetType = Create::class
            ),
            TargetType(
                discriminatorValue = "Update",
                targetType = Update::class
            )
        ]
    )
    fun activityViewToActivity(activityView: ActivityView): Activity
}
```

## TODO List

- [x] Publish to maven central repository
- [x] Add integration test
- [x] Add examples to project
- [x] Support ignore, rename and default values
- [x] Support nullable to non-nullable conversion
