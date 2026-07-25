---
name: reversing-network
description: Анализ OTA-протоколов, захват трафика, восстановление команд.
---

## Подходы

### Сбор трафика
- mitmproxy/Burp для HTTPS
- Wireshark/tcpdump для raw TCP/UDP
- Frida для перехвата SSL_read/SSL_write (если pinning)
- USB-сниффер для USB-протоколов

### Анализ протокола
- Идентификация маркера пакета (start byte / signature)
- Длина пакета: фиксированная или в заголовке
- CRC/checksum: XOR, CRC16, CRC32, Adler-32
- Поля: command ID, sequence number, payload length, payload

### OTA Update
- Запросить обновление → поймать manifest
- manifest содержит: version, URL, hash, signature
- Скачать и проанализировать блоки
- Искать downgrade bypass (старая версия без проверки)

### Восстановление API
- Глянуть трафик → восстановить структуру запросов
- Protobuf: найти .proto или восстановить через protobuf-inspector
- MessagePack/BSON: десериализация напрямую
- Кастомный binary protocol: написать парсер
