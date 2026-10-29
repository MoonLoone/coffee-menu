package ru.mipt.coffeemenu;

public final class PriceFormatter {

    private PriceFormatter() {
    }

    public static String format(int price) {
        var text = price + " ₽";
        return text;
    }
}
