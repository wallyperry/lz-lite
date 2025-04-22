package run.perry.lz.data.store

import kotlin.reflect.KProperty

class Store(val provider: StoreProvider) {
    interface Delegate<T> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }

    fun int(key: String, defaultValue: Int) = object : Delegate<Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            provider.getInt(key, defaultValue)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            provider.setInt(key, value)
        }
    }

    fun long(key: String, defaultValue: Long) = object : Delegate<Long> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            provider.getLong(key, defaultValue)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            provider.setLong(key, value)
        }
    }

    fun string(key: String, defaultValue: String) = object : Delegate<String> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            provider.getString(key, defaultValue)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            provider.setString(key, value)
        }
    }

    fun stringSet(key: String, defaultValue: Set<String>) = object : Delegate<Set<String>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            provider.getStringSet(key, defaultValue)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<String>) {
            provider.setStringSet(key, value)
        }
    }

    fun boolean(key: String, defaultValue: Boolean) = object : Delegate<Boolean> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            provider.getBoolean(key, defaultValue)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            provider.setBoolean(key, value)
        }
    }

    fun <T : Enum<T>> enum(key: String, defaultValue: T, values: Array<T>) = object : Delegate<T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            values.find { provider.getString(key, defaultValue.name) == it.name } ?: defaultValue

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            provider.setString(key, value.name)
        }
    }

    fun <T> typedString(key: String, from: (String) -> T?, to: (T?) -> String) =
        object : Delegate<T?> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) =
                from(provider.getString(key, to(null)))

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
                provider.setString(key, to(value))
            }
        }
}