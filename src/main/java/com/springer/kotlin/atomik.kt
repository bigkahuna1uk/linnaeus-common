package com.springer.kotlin

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty


operator fun <V> AtomicReference<V>.getValue(thisRef: Any?, property: KProperty<*>) = get()
operator fun <V> AtomicReference<V>.setValue(thisRef: Any?, property: KProperty<*>, newValue: V) = set(newValue)

operator fun AtomicBoolean.getValue(thisRef: Any?, property: KProperty<*>) = get()
operator fun AtomicBoolean.setValue(thisRef: Any?, property: KProperty<*>, newValue: Boolean) = set(newValue)

operator fun AtomicInteger.getValue(thisRef: Any?, property: KProperty<*>) = get()
operator fun AtomicInteger.setValue(thisRef: Any?, property: KProperty<*>, newValue: Int) = set(newValue)

operator fun AtomicLong.getValue(thisRef: Any?, property: KProperty<*>) = get()
operator fun AtomicLong.setValue(thisRef: Any?, property: KProperty<*>, newValue: Long) = set(newValue)


fun atomic(initialValue: Boolean) = AtomicBoolean(initialValue)
fun atomic(initialValue: Int) = AtomicInteger(initialValue)
fun atomic(initialValue: Long) = AtomicLong(initialValue)
fun <V> atomic(initialValue: V) = AtomicReference(initialValue)
fun <V> atomic() = AtomicReference<V?>(null)