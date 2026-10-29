package ru.mipt.coffeemenu

class Item(
    val id: String,
    val title: String,
    var subtitle: String?,
    val price: Int,
)

class SeasonalItem(id: String, title: String, price: Int) : Item(id, title, "сезонное", price)
