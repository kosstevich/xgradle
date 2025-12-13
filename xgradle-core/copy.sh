#!/bin/bash

# Проверка наличия xclip
if ! command -v xclip &> /dev/null; then
    echo "Ошибка: xclip не установлен. Установите его командой: sudo apt install xclip"
    exit 1
fi

# Поиск всех Java-файлов и сохранение в массив
mapfile -d $'\0' java_files < <(find . -type f -name "*.java" -print0 2>/dev/null)

# Проверка наличия файлов
if [ ${#java_files[@]} -eq 0 ]; then
    echo "Java-файлы не найдены"
    exit 1
fi

# Сборка содержимого с разделителями
output=""
for file in "${java_files[@]}"; do
    content=$(<"$file")
    output+="=== $file ===$content"
done

# Копирование в буфер обмена
echo -n "$output" | xclip -selection clipboard

# Отчет
echo "Скопировано ${#java_files[@]} Java-файлов в буфер обмена"
