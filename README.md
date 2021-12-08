# Rule Discord Bot

## Summary
A modularized discord bot written in Kotlin. This project aims to demonstrate usage of a few design decisions:
- Gradle with composite builds and convention plugins to increase developer productivity
- Coroutine wrappers around a reactive library (Discord4j) to facilitate best practices
- Combining multiple gradle builds into a single distribution unit (Docker) in a CD environment
- Using shared kotlin models across a fullstack, a server instance and an android client

## Module breakdown

Code sharing across the stack is demonstrated in the module relation diagram below. Each "rule" is a modular 
piece of logic that the chatbot packages as functionality. In the example of the "wellness" rule
we would like to expose an api to configure the rule, in this case to enable or disable for a particular 
server. A successful or failed configuration should be communicated to the client with a corresponding
response message. This is contained in a simple data class.

```kotlin
@Serializable
data class WellnessResponse(val message: String)
```

Since this is plain Kotlin (minus the serialization mechanism), we would like to reuse this across
our kotlin codebase without a dependency on any server or discord code. The models for this rule are then
split into a new composite build. Being isolated from any rule logic, this becomes a lightweight addition
to our client configuration app. The diagram below demonstrates how this is structured in gradle.

![Shared code across server and client](docs/images/rules.png)

