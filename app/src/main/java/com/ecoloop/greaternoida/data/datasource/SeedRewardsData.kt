package com.ecoloop.greaternoida.data.datasource

import com.ecoloop.greaternoida.domain.model.Reward

object SeedRewardsData {
    val rewards: List<Reward> = listOf(
        Reward(
            id = "rew_galgotias_01",
            title = "Galgotias Canteen Meal Voucher",
            description = "Get ₹50 discount on any meal thali or breakfast combo at Galgotias Central Campus Canteen.",
            partnerName = "Galgotias University Canteen, Knowledge Park II",
            pointsCost = 50,
            category = "Campus & Food",
            discountText = "₹50 OFF on Meal Thali",
            terms = "Valid on orders above ₹100. Show redemption code at cash counter. Valid for 30 days.",
            available = true
        ),
        Reward(
            id = "rew_sharda_02",
            title = "Sharda Cafe Beverage Discount",
            description = "Enjoy 25% off on all artisan coffee, smoothies, and cold pressed juices at Sharda University Campus Cafe.",
            partnerName = "Sharda Campus Cafe, Knowledge Park III",
            pointsCost = 35,
            category = "Campus & Food",
            discountText = "25% OFF on Beverages",
            terms = "Applicable once per visit. One coupon per student. Valid for 30 days.",
            available = true
        ),
        Reward(
            id = "rew_ansal_03",
            title = "Ansal Plaza Supermart Discount",
            description = "Flat ₹100 discount on eco-friendly home supplies and grocery purchases at Ansal Plaza Supermart.",
            partnerName = "Ansal Plaza Supermart, Pari Chowk",
            pointsCost = 100,
            category = "Groceries",
            discountText = "₹100 OFF on ₹500 Bill",
            terms = "Minimum purchase ₹500 required. Valid on all domestic grocery items. Valid for 45 days.",
            available = true
        ),
        Reward(
            id = "rew_pari_04",
            title = "Pari Chowk Academic Stationery",
            description = "Save ₹75 on recycled paper notebooks, pens, college project binding, and academic books.",
            partnerName = "Pari Chowk Book & Stationery Depot, Pari Chowk",
            pointsCost = 60,
            category = "Books & Stationery",
            discountText = "₹75 OFF on Stationery",
            terms = "Valid on purchases above ₹200. Valid for 60 days.",
            available = true
        ),
        Reward(
            id = "rew_beta_05",
            title = "Beta-1 Green Grocer Organic Box",
            description = "15% discount on organic vegetables, fresh fruits, and compostable kitchen essentials.",
            partnerName = "Beta-1 Green Grocer, Sector Beta 1 Market",
            pointsCost = 40,
            category = "Organic & Grocery",
            discountText = "15% OFF Organic Produce",
            terms = "Cannot be combined with other store offers. Valid for 30 days.",
            available = true
        )
    )
}
