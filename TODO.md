# 🚀 Сервер: HiTech 1.12.2 — План задач

## 📌 В процессе
- ✅ 🔍 Разобраться, **как выбираются координаты для посадки на планеты**

  **Разбор**:

  📦 Основная логика посадки реализована в классе `EntityRocket` (наследник `EntityRocketBase`).

  🔽 Пошагово:

  1. Ракета определяет измерение назначения:
     ```java
     destinationDimId = storage.getDestinationDimId(...);
     ```

  2. Получает координаты посадки:
     ```java
     Vector3F<Float> pos = storage.getDestinationCoordinates(destinationDimId, true);
     ```

  3. Метод `getDestinationCoordinates(...)` реализован в `StorageChunk`:
     - Ищет `TileGuidanceComputer` на борту ракеты.
     - Делегирует вызов `getLandingLocation(...)`.

  4. Метод `getLandingLocation(...)` (`TileGuidanceComputer`):
     - В зависимости от вставленного чипа:
       - 📍 `ItemLinker`: координаты берутся из NBT (связанная площадка)
       - 🛰 `ItemStationChip`: координаты сохранены в чипе как `takeoffCoords`
       - 🪨 `ItemAsteroidChip`: используются ранее сохранённые `landingPos`
       - ❌ `ItemSatelliteChip`: возвращает `null` (неподдерживается)

  5. При наличии координат, ракета телепортируется:
     ```java
     setPositionAndUpdate(pos.x, getEntryHeight(destDimId), pos.z);
     ```

  📌 Без подходящего чипа посадка невозможна (`null`).

---

- ✅ 🔬 Исследовать, **как считается топливо и как работает система расхода**

  **Разбор**:

  📦 Главная логика реализована в `EntityRocket` + `StatsRocket`.

  🔽 Механика:

  1. Расход происходит каждую единицу времени (тик/шаг полёта):
     ```java
     setFuelAmount(getFuelType(), getFuelAmount(...) - getFuelConsumptionRate(...));
     ```

  2. Расход зависит от:
     - типа топлива (`LIQUID_MONOPROPELLANT`, `LIQUID_BIPROPELLANT`)
     - конфигов (`ARConfiguration`)
     - настроек в `getFuelConsumptionRate(...)`

  3. Вся информация о запасе и ёмкости хранится в `StatsRocket`:
     - `getFuelAmount()`
     - `getFuelCapacity()`
     - `addFuel(...)`

  4. `FuelRegistry` определяет, какие жидкости считаются топливом, и сколько дают энергии.

  ❗ Если топлива нет — ракета не взлетает или аварийно садится.

---

- ✅ 🛢 Изучить, **как работает топливная станция (TileFuelingStation)**

  **Разбор**:

  📦 Основная логика заправки реализована в методе `performFunction()`.

  🔽 Что происходит:

  1. Станция проверяет, есть ли жидкость в своём баке (`tank.getFluid()`)
  2. Определяет тип жидкости через `FuelRegistry`
  3. Если ракета не имеет привязки по типу топлива — устанавливает его:
     - монопропеллент
     - бипропеллент / окислитель
     - ядерная жидкость
  4. Если тип уже установлен — заправляет только им
  5. Вызов:
     ```java
     linkedRocket.addFuel(FuelType, amount);
     ```

  📌 Станция заправляет ракету **только одним типом топлива**, определяемым **первой залитой жидкостью**.

---

- ✅ 🔬 Исследовать, **ARConfiguration и как он влияет на топливо**

  **Разбор (дополнение)**:

  🛠 Конфигурация `ARConfiguration` управляет глобальными параметрами расхода топлива:

  | Параметр | Значение | Назначение |
  |----------|----------|------------|
  | `fuelPointsPer10Mb` | `10` | Сколько очков даёт 10 mB |
  | `rocketRequireFuel` | `true` | Требуется ли топливо вообще |
  | `canBeFueledByHand` | `true` | Можно ли вручную заливать |
  | `fuelCapacityMultiplier` | `1.0` | Множитель для бака |
  | `gravityAffectsFuel` | `true` | Увеличивает расход на тяжёлых планетах |
  | `liquidMonopropellant`, `liquidBipropellantFuel`, `...` | список жидкостей с очками |

  🔁 Всё это используется в `FuelRegistry` и `EntityRocket`, чтобы рассчитать:
  - Сколько очков даёт жидкость
  - Сколько нужно потратить за тик
  - Учитывается ли гравитация

  📌 Всё настраивается в `config/advancedRocketry/advancedRocketry.cfg`

---

- ✅ 🛠 Пройтись по конфигам и проверить все опции
- ✅ 🧹 Почистить клиентскую часть: убрать моды/данные, неиспользуемые на сервере

## ✅ Готово
- [x] Удалены все `logger.info` из `DimensionManager`
- [x] Убран лог спам от `GenLayerRiverMixWrapper`
- [x] Синхронизированы версии `ConfigAnytime` (2.0 сервер / 2.0 клиент)

## 🧠 Идеи на будущее
- Думаем..
