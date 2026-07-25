---
name: commit-message
description: Генерация сообщений коммита по Conventional Commits.
---

## Формат

```
<type>(<scope>): <short summary>

<body>
<footer>
```

## Типы
- `feat` — новая функциональность
- `fix` — исправление бага
- `refactor` — рефакторинг без изменения поведения
- `test` — добавление/исправление тестов
- `docs` — документация
- `chore` — служебные изменения (зависимости, CI)
- `perf` — оптимизация производительности
- `security` — исправление уязвимости

## Правила
- Summary до 72 символов, без точки в конце
- В body объяснить ЧТО и ПОЧЕМУ, а не КАК
- Если есть breaking changes: `BREAKING CHANGE:` в footer
- Если закрывает issue: `Closes #123` в footer
