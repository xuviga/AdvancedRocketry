# Changelog

Все значимые изменения модов и конфигурации сервера Minecraft будут описаны здесь.  
---

## [1.3.0] - 2025-04-15

### 🆕 Добавлено
- Установлен плагин **ConsoleSpamFix** и настроен на фильтрацию:
  - climateControl, UUID, Mohist, ProjectRed и прочих спам-сообщений.
- В конфигурации **GeographiCraft** добавлено:
  - Расширенные фильтры биомов, улучшенная генерация пляжей.

### ⚙ Изменено
- Очищен логгер из исходника `GenLayerRiverMixWrapper` (убрано `logger.info`)
- Полностью удалены логи из `DimensionManager` (мод GeographiCraft)
- Настроены фильтры в `ConsoleSpamFix` для подавления:
  - `[INFO]: Ocean true ...`
  - `UUID of player`
  - `Client attempting to join with`

### 🐞 Исправлено
- Конфликт версий `ConfigAnytime` (на сервере была 2.0, у клиента 3.0) — синхронизировано.
- Ошибка с повреждённым модом `StellarCore-1.5.21.jar` — мод заменён на рабочую версию.
- Удалены краш-логи, связанные с `BlockDynamicLiquid.func_180650_b` в Advanced Rocketry.

---

## [1.2.0] - 2025-04-10

### ⚙ Изменено
- Перенастроен `VintageFix` на сервер: удалены клиентские и конфликтующие функции.

### 🐞 Исправлено
- Краш при генерации мира из-за `TileInvHatch` — добавлен недостающий маппинг.
- Исправлена ошибка с `getX()`, `getZ()` в `TileEntityNuclearReactorElectric`.

---