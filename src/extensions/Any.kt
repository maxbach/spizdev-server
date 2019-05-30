package extensions

import ru.touchin.utils.GsonStore

fun Any.toJson(): String = GsonStore.gson.toJson(this)