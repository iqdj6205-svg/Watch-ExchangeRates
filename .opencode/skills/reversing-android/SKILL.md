---
name: reversing-android
description: Реверс-инжиниринг Android: APK, smali, JNI, обфускация.
---

## Декомпиляция APK

1. `apktool d target.apk` — ресурсы + smali
2. `dex2jar` / `jadx` / `jeb` — декомпиляция в Java
3. Сравнить исходный smali и Java — найти модификации

## JNI / Native код
- `lib/*.so` — ELF под ARM/ARM64/x86
- `strings -n 10` — найти имена Java-методов (JNINativeMethod)
- Искать `JNI_OnLoad`, `RegisterNatives`
- Если stripped — искать строки с сигнатурами методов

## Обфускация
- ProGuard — переименованные классы (a, b, c)
- Obfuscator в smali — запутанный control flow
- Шифрование строк — найти функцию дешифровки
- Reflection — вызовы через `Class.forName`, `Method.invoke`

## Анализ трафика
- Прокси: Burp, Frida, mitmproxy
- SSL pinning — обходить через Frida/XPosed
- Protobuf — найти .proto или восстановить
