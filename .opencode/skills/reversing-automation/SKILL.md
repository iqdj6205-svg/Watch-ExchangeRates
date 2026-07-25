---
name: reversing-automation
description: Автоматизация анализа: Ghidra/IDA скрипты, пайплайны, CI.
---

## Ghidra Scripting (Python)

```python
# Получить все функции и их размер
from ghidra.program.model.listing import Function
fm = currentProgram.getFunctionManager()
for f in fm.getFunctions(True):
    size = f.getBody().getNumAddresses()
    print(f"{f.getName()}: {size} bytes")

# Поиск константы
from ghidra.app.util import XorInput
# Xor поиск: найти все инструкции с константой 0x9e3779b9
```

## IDA Python

```python
# Экспорт всех функций
for ea in Functions():
    name = GetFunctionName(ea)
    size = GetFunctionAttr(ea, FUNCATTR_END) - ea
    print(f"{name} @ {hex(ea)} size={size}")

# Поиск всех XOR с константой
import idc, idautils
for head in idautils.Heads():
    if idc.print_insn_mnem(head) == 'xor':
        op = idc.print_operand(head, 1)
        if '9e3779b9' in op:
            print(f"TEA delta @ {hex(head)}")
```

## CI/CD для RE
- GitHub Actions: авто-распаковка каждой версии прошивки
- Telegram бот: новая версия → diff → оповещение
- Бенчмарк: сравнение энтропии, сигнатуры, размеры
- Возвращать структурированный JSON (не только дамп)
