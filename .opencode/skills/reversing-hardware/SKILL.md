---
name: reversing-hardware
description: Работа с железом: UART, JTAG, SPI, дамп flash.
---

## Интерфейсы

### UART
- 3.3V, 115200/57600/9600 baud — самые частые
- Найти на плате: TX, RX, GND (обычно 3-пиновый)
- Подключиться: USB-UART адаптер → minicom/puTTY
- Ловить boot log, shell, debug вывод
- Если логи есть — искать команды и бэкдоры

### JTAG/SWD
- Стандарт для дебагга embedded
- Инструменты: OpenOCD, JLink, ST-Link
- Чтение flash через OpenOCD: `flash read_bank 0 dump.bin 0 0x100000`
- Если locked (RDP) — искать bootrom exploit

### SPI Flash
- SOIC-8 чип: CS, MISO, MOSI, CLK, VCC, GND
- Дамп: flashrom / buspirate / raspberry pi
- Если прошивка читается — сохранить образ
- Если не читается — проверить WP#/HOLD пины

### Анализ платы
- Фотографировать плату со всех сторон
- Искать test points (TP), u.FL разъёмы
- UART часто подписан: TX, RX, GND, VCC
- Модели чипов гуглить: datasheet → undocumented commands
