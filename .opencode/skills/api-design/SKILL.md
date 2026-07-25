---
name: api-design
description: Проверка консистентности REST/gRPC API.
---

## REST
- Ресурсы во множественном числе: `/users`, `/orders`
- HTTP методы по смыслу: GET/читаем, POST/создаём, PUT/заменяем, PATCH/обновляем, DELETE/удаляем
- Ошибки в едином формате: `{ error, message, code, details }`
- Пагинация: `{ data, nextCursor, total }`
- Версионирование: `/v1/` или header `Accept-Version`
- Одинаковые поля в ответе (snake_case или camelCase — но не мешанина)

## Безопасность
- rate limiting, CORS, CSRF
- Валидация всех входных параметров
- Ограничение размера тела запроса

## gRPC
- Статус-коды по смыслу
- batching где применимо
- Streaming для больших данных
- backward-compatible поля (не менять номера)
