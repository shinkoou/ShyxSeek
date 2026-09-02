# ShyxSeek

Private, extensible Android AI assistant base.

## v0.1
- Kotlin + Compose + Material 3 + Hilt
- Room conversations, messages, knowledge, memory and projects
- local hybrid RAG
- `AIProvider` abstraction
- Fake offline provider
- OpenAI-compatible streaming chat provider
- encrypted API key storage with Android Keystore
- ToolRegistry with local knowledge search
- AMOLED-black UI

## Build
Requires JDK 17, Android SDK 36 and Gradle 8.13 via wrapper/CI. Run `./gradlew :app:assembleDebug`.

## Security
No keys in source, no analytics, no ads. Cloud usage is opt-in through Settings.

## Roadmap
Files/PDF/vision/web research → connectors/automations → voice/artifacts → local LLM/plugins.
