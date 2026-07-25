---
name: reversing-anti
description: Детект и обход обфускации, анти-дебага, упаковщиков.
---

## Детект упаковки
- Высокая энтропия ВСЕГО файла (не только секции) — packed
- Мало строк (< 10 осмысленных)
- Таблица импорта пустая или только kernel32/LoadLibrary
- Точка входа типичная для UPX/MPRESS/VMProtect
- Разница между file size и virtual size > 50%

## Обфускация
### Control Flow
- Много безусловных jmp
- opaque predicates (всегда true/false условие)
- Вызовы через таблицы указателей
- Анализ: Unicorn/Triton для динамической размотки

### String obfuscation
- Строки XOR/shift на лету при первом обращении
- Frida: хукать все String конструкторы
- Unicorn: эмулировать функцию дешифровки строк

### Anti-debug
- `ptrace(PTRACE_TRACEME)` — только один дебаггер
- `IsDebuggerPresent` / `NtQueryInformationProcess`
- Проверка `int 0x2d`, `int 0x1` (Linux)
- Timming: `rdtsc` разница до/после
- Обход: NOP-ить, патчить jump, Frida `Process.setExceptionHandler`

## Упаковщики
- UPX: `upx -d target` или найти UPX! маркер
- MPRESS: поиск OEP по типичным аргументам Push/Ret
- VMProtect: потребуется Unicorn/Triton для эмуляции виртуальной машины
- Themida: сложно, лучше дамп после загрузки
