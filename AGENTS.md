# Codex Subagents For CrossfitArmyJym

Этот репозиторий использует небольшой набор специализированных субагентов Codex. Их профили лежат в `.codex/subagents`.

Перед началом работы изучай `context/activeContext.md`, `context/productContext.md`, `context/progress.md`, `context/projectbrief.md`, `context/systemPatterns.md` и `context/techContext.md`, если задача затрагивает архитектуру, продуктовую логику или текущий план.

## Когда привлекать субагентов

- Новая функциональность или рефакторинг: сначала `android_architect`, затем реализация, затем `qa_test_writer`.
- Ошибка приложения, ANR, нестабильное поведение, сеть, Room, Lifecycle: сначала `bug_investigator`, затем точечное исправление, затем `qa_test_writer`.
- Ошибка сборки, Gradle, зависимости, AGP, CI: сначала `gradle_build_doctor`.
- Авторизация, пользовательские данные, сеть, разрешения, Supabase policies, токены: дополнительно `security_privacy_reviewer`.
- Release, internal testing build, Google Play, release APK/AAB: `release_reviewer`, затем `security_privacy_reviewer`.

## Общие правила

- Не создавай нового субагента под каждый экран или функцию.
- Перед советами изучай конкретный код текущего проекта.
- Предпочитай минимальные безопасные изменения.
- Не добавляй библиотеки без сильного обоснования.
- Не меняй Gradle-конфигурацию без объяснения причины.
- Всегда перечисляй затронутые файлы и способ проверки.
- Вопросы задавай только если без ответа невозможно безопасно продолжить.
- Учитывай существующий стек: Java, Android XML Views, MVVM, Retrofit, Room, Supabase/PostgREST, Gradle.
- Не откатывай чужие изменения в рабочем дереве.

## Профили

- `.codex/subagents/android_architect.md`
- `.codex/subagents/gradle_build_doctor.md`
- `.codex/subagents/qa_test_writer.md`
- `.codex/subagents/bug_investigator.md`
- `.codex/subagents/security_privacy_reviewer.md`
- `.codex/subagents/release_reviewer.md`

