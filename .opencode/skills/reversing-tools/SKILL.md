---
name: reversing-tools
description: Чеклист инструментов и команд для реверс-инжиниринга.
---

## Команды

```
# Анализ файла
file target.bin
xxd target.bin | head -20
hexdump -C target.bin | head -20

# Энтропия
python3 -c "import sys,math; d=open(sys.argv[1],'rb').read(); e=sum(-(c/len(d))*math.log2(c/len(d)) for c in [d.count(b) for b in range(256)] if c); print(f'{e:.2f} bits')"

# Сигнатуры
binwalk -Me target.bin
strings -n 8 target.bin
strings -e l -n 8 target.bin   # UTF-16

# Поиск в бинарнике
rg --binary -o $'\x63\x7c\x77\x7b' target.bin

# Разделение
dd if=target.bin bs=1 skip=OFFSET count=SIZE of=output.bin
```

## Инструменты

| Инструмент | Назначение |
|------------|------------|
| `binwalk` | Анализ и извлечение вложенных файлов |
| `apktool` | Распаковка/сборка APK |
| `jadx` | Декомпиляция DEX в Java |
| `Ghidra` | Дизассемблер + decompiler (SRE) |
| `IDA Pro` | Продвинутый дизассемблер |
| `uncompyle6` / `decompyle3` | Python .pyc |
| `frida` | Hook-инжекция |
| `x64dbg` | Windows debugger |
| `ImHex` | Hex-редактор с паттернами |
