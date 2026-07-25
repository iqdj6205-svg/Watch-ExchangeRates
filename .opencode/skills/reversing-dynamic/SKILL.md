---
name: reversing-dynamic
description: Динамический анализ: дебаггинг, логи, эмуляция, Frida.
---

## Инструменты

- Frida — хукинг на лету, без пересборки
- `frida-trace` — трассировка функций
- QEMU/Tegra — эмуляция прошивок
- Unicorn — эмуляция отдельных инструкций/блоков
- Angr — символьное выполнение для поиска path

## Frida скрипты

```javascript
// Хук функции
Interceptor.attach(Module.findExportByName(null, "funcName"), {
  onEnter(args) { console.log(args[0].toInt32()); },
  onLeave(retval) { console.log(retval); }
});
```

## Логи

- logcat для Android
- strace/ltrace для Linux
- API monitor в Burp/proxyman
- Сравнение логов с/без модификации

## Unicorn

- Эмуляция одного алгоритма без запуска всей прошивки
- Перехват вызовов `memcpy`, `malloc` для отслеживания данных
- Снэпшоты: сохранить состояние, выполнить, откатить
