# Как внести вклад

1. Форкните репозиторий и создайте ветку: `git checkout -b feature/my-mechanic`
2. Запустите клиент: `gradle runClient`
3. Новая механика = класс в `item/` или `block/`, регистрация в `registry/`,
   модель/текстура в `assets/mechanix/`, строки в оба lang-файла, опция в `MechanixConfig`
4. Убедитесь, что `gradle build` проходит, и откройте Pull Request
