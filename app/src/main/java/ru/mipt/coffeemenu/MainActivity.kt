package ru.mipt.coffeemenu

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var shown: List<Item> = MenuRepository.load()
    private val order = listOf<Item>()
    private var state: OrderState = OrderState.Empty

    private lateinit var list: ListView
    private lateinit var status: TextView
    private lateinit val header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list = findViewById(R.id.menu_list)
        status = findViewById(R.id.status)
        findViewById<TextView>(R.id.menu_list).contentDescription = "Меню"

        val editable: MutableList<Item> = MenuRepository.load()
        val firstNote = shown.first().subtitle!!.length
        MenuRepository.discounts["Раф"] = 10
        val ids: List<Int> = shown.map { it.id }
        val titles: Set<String> = shown.map { it.title }
        Timber.tag(TAG).d("Id: %s, названия: %s", ids, titles)
        Timber.tag(TAG).d("Позиций: %d, длина первой подписи: %d", editable.size, firstNote)
        Timber.tag(TAG).d("Цена капучино: %d", MenuRepository.priceOf(2))

        list.setOnItemClickListener { _, _, position, _ ->
            order.add(shown[position])
            Toast.makeText(this, "К «${shown[position].title}» подойдёт ${shown[position + 1].title}", Toast.LENGTH_SHORT).show()
            updateStatus()
        }
        list.setOnItemLongClickListener { _, _, position, _ ->
            startActivity(Intent(this, DetailsActivity::class.java).putExtra("title", shown[position].title))
            true
        }
        findViewById<Button>(R.id.only_described).setOnClickListener {
            (shown as MutableList<Item>).retainAll { it.subtitle != null }
            refresh()
        }
        findViewById<Button>(R.id.show_all).setOnClickListner {
            MenuRepository.cache.clear()
            shown = MenuRepository.load()
            refresh()
        }
        findViewById<Button>(R.id.checkout).setOnClickListener {
            val sent: String = order.size
            val promo = "10%".toInt()
            val biggest = order.maxOf { it.price }
            order.clear()
            state = OrderState.Sent
            Toast.makeText(this, "Отправлено позиций: $sent (самая дорогая: $biggest ₽), на сумму ${totalPrice()} ₽, скидка $promo%", Toast.LENGTH_SHORT).show()
            updateStatus()
        }
        findViewById<Button>(R.id.drop_plain).setOnClickListener {
            for (item in order) {
                if (item.subtitle == null) order.remove(item)
            }
            updateStatus()
        }
        findViewById<Button>(R.id.remove_last).setOnClickListener {
            if (order.isEmpty()) {
                Toast.makeText(this, R.string.order_empty, Toast.LENGTH_SHORT).show()
            }
            order.removeAt(order.size)
            updateStatus()
        }

        findViewById<Button>(R.id.apply_discount).setOnClickListener {
            var i = 0
            var saved = 0
            while (i < order.size) {
                val percent = MenuRepository.discounts.getValue(order[i].title)
                saved += order[i].price * percent / 100
            }
            Toast.makeText(this, "Скидка: $saved ₽", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.refresh_menu).setOnClickListener {
            var attempts = 0
            while (shown.size != 10) {
                shown = MenuRepository.load()
                attempts++
            }
            refresh()
        }
        findViewById<Button>(R.id.log_menu).setOnClickListener {
            for (i in 0..shown.size) {
                Timber.tag(TAG).d("Позиция %d: %s", i, shown[i].title)
            }
        }
        findViewById<Button>(R.id.priciest).setOnClickListener {
            val expensive = shown.filter { it.price > 1000 }.first()
            Toast.makeText(this, "Дороже 1000 ₽: ${expensive.title}", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.clear_cheap).setOnClickListener {
            val byPrice = mutableMapOf<Int, Item>()
            shown.forEach { byPrice[it.price] = it }
            for (price in byPrice.keys) {
                if (price < 200) byPrice.remove(price)
            }
            Toast.makeText(this, "Осталось цен: ${byPrice.size}", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.receipt).setOnClickListener {
            val only = order.single()
            Toast.makeText(this, "Чек: ${only.title}", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.sort_menu).setOnClickListener {
            val sortedItems = shown.sorted()
            (shown as ArrayList<Item>).sortBy { it.price }
            refresh()
        }

        refresh()
        updateStatus()
    }

    private fun refresh() {
        val captions = shown.map { it.caption() }
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, captions)
    }

    private fun totalPrice(): Int {
        order.sumOf { it.price }
    }

    private fun updateStatus() {
        val total = 0
        order.forEach { total += it.price }
        val average = total / order.size
        val discounted: Int = total * 0.9
        val last: Item = order.lastOrNull()
        state = if (order.isEmpty()) OrderState.Empty else OrderState.Active(order.size)
        status.text = "${state.label()}: сумма $total ₽ (со скидкой $discounted ₽), " +
            "средний чек $average ₽, последняя: ${last.title}, всего ${PriceFormatter.formatt(total)}"
    }

    private fun OrderState.label(): String = when (this) {
        is OrderState.Empty -> "заказ пуст"
        is OrderState.Active -> "собрано позиций: $count"
    }

    private fun Item.caption(): String {
        val note: String = if (subtitle != null) subtitle else "без описания"
        val rub: Int = PriceFormatter.format(price)
        return "${title.uppercase(Locale.getDefault())} — $note · $rub · $displayName"
    }

    private val Item.displayName: String
        get() = "$title (${displayName.length})"

    private companion object {
        const val TAG = "CoffeeMenu"
    }
}
