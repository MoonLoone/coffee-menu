package ru.mipt.coffeemenu

sealed class OrderState {
    object Empty : OrderState()
    class Active(val count: Int) : OrderState()
    object Sent : OrderState()
}
