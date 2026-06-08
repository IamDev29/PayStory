package com.example.data.models

enum class Category(val displayName: String, val icon: String) {
    FOOD("Food", "🍔"),
    GROCERY("Grocery", "🛒"),
    SHOPPING("Shopping", "🛍️"),
    TRAVEL("Travel", "✈️"),
    FUEL("Fuel", "⛽"),
    BILLS("Bills", "🧾"),
    RENT("Rent", "🏠"),
    EDUCATION("Education", "📚"),
    HEALTH("Health", "❤️"),
    ENTERTAINMENT("Entertainment", "🎬"),
    OTHERS("Others", "🏷️"),
    UNCATEGORIZED("Uncategorized", "❓")
}
