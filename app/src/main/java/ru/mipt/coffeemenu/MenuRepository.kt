package ru.mipt.coffeemenu

object MenuRepository {
    private val cache = mutableMapOf<String, Item>()

    fun load(): List<Item> = listOf(
        Item("1", "Эспрессо", null, 120),
        Item("2", "Капучино", "молоко 150 мл", 220),
        Item("3", "Латте", "молоко 250 мл", 240),
        Item("4", "Раф", 280),
    )

    val discounts = mapOf("Эспрессо" to 5, "Капучино" to 10, "Латте" to 10)

    fun priceOf(id: String): Int = load().first { it.id == id }.price
}
