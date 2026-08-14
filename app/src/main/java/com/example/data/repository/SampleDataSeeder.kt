package com.example.data.repository

import com.example.data.local.ShopDao
import com.example.data.model.AccessoryItem
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.MobileItem
import com.example.data.model.RateItem
import com.example.data.model.RepairJob
import com.example.data.model.ShopProfile

object SampleDataSeeder {

    suspend fun seedSampleData(dao: ShopDao) {
        val now = System.currentTimeMillis()

        // Shop Profile
        dao.saveShopProfile(
            ShopProfile(
                id = 1,
                ownerName = "Muhammad Kashif",
                shopName = "KASHIF MOBILE AND REPAIR",
                phoneNumber = "+966 50 123 4567",
                whatsappNumber = "+966 50 123 4567",
                emailAddress = "m.kashifkuar5827@gmail.com",
                shopAddress = "Shop #12, Al-Batha Mobile Market",
                city = "Riyadh",
                notes = "Wholesale & Retail Mobile Phones, Genuine Accessories, Fast Hardware & Software Repair Solutions",
                currency = "SAR",
                language = "en",
                invoiceTerms = "1. Checked warranty only on hardware repairs.\n2. Please collect mobile within 15 days.\n3. Original receipt is mandatory for collection."
            )
        )

        // Mobiles
        val mobiles = listOf(
            MobileItem(
                brand = "Apple",
                model = "iPhone 15 Pro Max",
                ram = "8GB",
                storage = "256GB",
                color = "Natural Titanium",
                imei = "354892019482019",
                barcode = "KMR-IPHONE15PM",
                purchasePrice = 3900.0,
                salePrice = 4350.0,
                quantity = 4,
                dateAdded = now - 86400000L * 2,
                notes = "Brand New, Official Apple Warranty"
            ),
            MobileItem(
                brand = "Samsung",
                model = "Galaxy S24 Ultra",
                ram = "12GB",
                storage = "512GB",
                color = "Titanium Black",
                imei = "359128038291038",
                barcode = "KMR-SAMS24U",
                purchasePrice = 3600.0,
                salePrice = 4100.0,
                quantity = 3,
                dateAdded = now - 86400000L * 3,
                notes = "Samsung Official Store Stock"
            ),
            MobileItem(
                brand = "Xiaomi",
                model = "Redmi Note 13 Pro+",
                ram = "12GB",
                storage = "256GB",
                color = "Midnight Black",
                imei = "864920192849102",
                barcode = "KMR-REDMI13P",
                purchasePrice = 1100.0,
                salePrice = 1350.0,
                quantity = 6,
                dateAdded = now - 86400000L,
                notes = "5G Supported, 120W HyperCharge"
            ),
            MobileItem(
                brand = "Infinix",
                model = "Zero 30 5G",
                ram = "8GB",
                storage = "256GB",
                color = "Rome Green",
                imei = "358192039182049",
                barcode = "KMR-INFINIXZ30",
                purchasePrice = 750.0,
                salePrice = 920.0,
                quantity = 5,
                dateAdded = now,
                notes = "Popular budget choice with 4K Front Camera"
            )
        )
        dao.insertMobilesBulk(mobiles)

        // Accessories
        val accessories = listOf(
            AccessoryItem(
                name = "Apple 20W USB-C Fast Power Adapter",
                category = "Charger",
                barcode = "KMR-ACC-CHG20W",
                purchasePrice = 45.0,
                salePrice = 75.0,
                quantity = 25,
                dateAdded = now - 86400000L * 5,
                notes = "Original Box Pack"
            ),
            AccessoryItem(
                name = "Samsung 45W Super Fast Type-C Charger",
                category = "Charger",
                barcode = "KMR-ACC-CHG45W",
                purchasePrice = 55.0,
                salePrice = 90.0,
                quantity = 18,
                dateAdded = now - 86400000L * 4,
                notes = "With 5A C-to-C Cable"
            ),
            AccessoryItem(
                name = "9D Matte Privacy Ceramic Glass Protector",
                category = "Glass Protector",
                barcode = "KMR-ACC-GLASS9D",
                purchasePrice = 5.0,
                salePrice = 25.0,
                quantity = 50,
                dateAdded = now - 86400000L * 3,
                notes = "Unbreakable flexible ceramic"
            ),
            AccessoryItem(
                name = "Anker 10,000mAh Magnetic Power Bank",
                category = "Power Bank",
                barcode = "KMR-ACC-PBANK10K",
                purchasePrice = 95.0,
                salePrice = 145.0,
                quantity = 12,
                dateAdded = now - 86400000L * 2,
                notes = "MagSafe Compatible with Kickstand"
            ),
            AccessoryItem(
                name = "iPhone 15 Pro Luxury Armor Silicone Case",
                category = "Mobile Cover",
                barcode = "KMR-ACC-CASE15P",
                purchasePrice = 12.0,
                salePrice = 35.0,
                quantity = 30,
                dateAdded = now,
                notes = "Drop protection with camera ring"
            )
        )
        dao.insertAccessoriesBulk(accessories)

        // Repair Jobs
        val repairs = listOf(
            RepairJob(
                jobCode = "REP-1001",
                customerName = "Tariq Mehmood",
                customerPhone = "+966 55 987 6543",
                mobileBrand = "Apple",
                mobileModel = "iPhone 13",
                imei = "354192019482019",
                barcode = "KMR-REP-1001",
                customerProblem = "Display broken after drop. Touch not responding.",
                repairDetails = "Replaced with Original OLED screen and new waterproof seal",
                repairService = "Display Replacement",
                repairCost = 350.0,
                advancePayment = 100.0,
                remainingPayment = 250.0,
                dateReceived = now - 86400000L,
                expectedDeliveryDate = now + 86400000L,
                status = "REPAIRING",
                notes = "Keep old damaged display for customer"
            ),
            RepairJob(
                jobCode = "REP-1002",
                customerName = "Ahmad Al-Ghamdi",
                customerPhone = "+966 50 443 2211",
                mobileBrand = "Samsung",
                mobileModel = "Galaxy A54",
                imei = "358129038291002",
                barcode = "KMR-REP-1002",
                customerProblem = "Not charging / Loose Type-C socket",
                repairDetails = "Soldered original charging port flex PCB",
                repairService = "Charging Jack",
                repairCost = 80.0,
                advancePayment = 80.0,
                remainingPayment = 0.0,
                dateReceived = now - 86400000L * 2,
                expectedDeliveryDate = now - 86400000L,
                status = "COMPLETED",
                notes = "Tested fast charging 15W OK"
            ),
            RepairJob(
                jobCode = "REP-1003",
                customerName = "Usman Farooq",
                customerPhone = "+966 54 332 1199",
                mobileBrand = "Xiaomi",
                mobileModel = "Poco X3 Pro",
                imei = "864192039182049",
                barcode = "KMR-REP-1003",
                customerProblem = "Battery draining fast in 2 hours",
                repairDetails = "New 5160mAh high capacity battery replacement",
                repairService = "Battery Replacement",
                repairCost = 120.0,
                advancePayment = 50.0,
                remainingPayment = 70.0,
                dateReceived = now,
                expectedDeliveryDate = now + 86400000L * 2,
                status = "PENDING",
                notes = "Customer requested 3 months warranty"
            )
        )
        dao.insertRepairsBulk(repairs)

        // Rates
        val rates = listOf(
            // Repair Rates
            RateItem(type = "REPAIR_SERVICE", title = "Display Replacement (OLED/AMOLED)", category = "Screen", standardRate = 280.0, purchaseRate = 180.0, sellingRate = 280.0, notes = "Includes installation & 1 week test warranty"),
            RateItem(type = "REPAIR_SERVICE", title = "Display Replacement (LCD/TFT)", category = "Screen", standardRate = 140.0, purchaseRate = 80.0, sellingRate = 140.0, notes = "Budget aftermarket panel"),
            RateItem(type = "REPAIR_SERVICE", title = "Battery Replacement", category = "Battery", standardRate = 110.0, purchaseRate = 50.0, sellingRate = 110.0, notes = "Original grade battery with IC"),
            RateItem(type = "REPAIR_SERVICE", title = "Charging Jack / Port Flex", category = "Charging", standardRate = 75.0, purchaseRate = 20.0, sellingRate = 75.0, notes = "Mic and fast charge tested"),
            RateItem(type = "REPAIR_SERVICE", title = "Software Flash / Unbrick / FRP Unlock", category = "Software", standardRate = 80.0, purchaseRate = 0.0, sellingRate = 80.0, notes = "Official firmware flashing"),
            RateItem(type = "REPAIR_SERVICE", title = "Speaker / Earpiece / Mic Replacement", category = "Audio", standardRate = 60.0, purchaseRate = 15.0, sellingRate = 60.0, notes = "Clear sound guarantee"),
            RateItem(type = "REPAIR_SERVICE", title = "Back Glass / Rear Cover Laser Replacement", category = "Body", standardRate = 130.0, purchaseRate = 45.0, sellingRate = 130.0, notes = "OEM fitment finish"),
            RateItem(type = "REPAIR_SERVICE", title = "Motherboard IC Work / Shorting Removal", category = "Motherboard", standardRate = 220.0, purchaseRate = 60.0, sellingRate = 220.0, notes = "Micro-soldering inspection"),
            RateItem(type = "REPAIR_SERVICE", title = "Water Damage Ultrasonic Cleaning", category = "Diagnostics", standardRate = 100.0, purchaseRate = 10.0, sellingRate = 100.0, notes = "No fix no fee policy"),

            // Mobile Rates
            RateItem(type = "MOBILE", title = "iPhone 15 Pro Max 256GB", category = "Apple", ram = "8GB", storage = "256GB", color = "Natural Titanium", purchaseRate = 3900.0, sellingRate = 4350.0, notes = "Official standard market price"),
            RateItem(type = "MOBILE", title = "Galaxy S24 Ultra 512GB", category = "Samsung", ram = "12GB", storage = "512GB", color = "Titanium Black", purchaseRate = 3600.0, sellingRate = 4100.0, notes = "Snapdragon version"),
            RateItem(type = "MOBILE", title = "Redmi Note 13 Pro+ 256GB", category = "Xiaomi", ram = "12GB", storage = "256GB", color = "Black", purchaseRate = 1100.0, sellingRate = 1350.0, notes = "5G Global version"),

            // Accessory Rates
            RateItem(type = "ACCESSORY", title = "20W USB-C Fast Charger", category = "Charger", purchaseRate = 45.0, sellingRate = 75.0, notes = "Wholesale rate"),
            RateItem(type = "ACCESSORY", title = "Super Fast 45W Type-C Adapter", category = "Charger", purchaseRate = 55.0, sellingRate = 90.0, notes = "Retail price"),
            RateItem(type = "ACCESSORY", title = "9D Matte Ceramic Screen Protector", category = "Glass Protector", purchaseRate = 5.0, sellingRate = 25.0, notes = "Includes fitting service"),
            RateItem(type = "ACCESSORY", title = "10000mAh MagSafe Power Bank", category = "Power Bank", purchaseRate = 95.0, sellingRate = 145.0, notes = "Fast wireless charging")
        )
        dao.insertRatesBulk(rates)

        // Customers
        val customers = listOf(
            Customer(name = "Tariq Mehmood", phone = "+966 55 987 6543", address = "Olaya District, Riyadh", notes = "Regular repair customer"),
            Customer(name = "Ahmad Al-Ghamdi", phone = "+966 50 443 2211", address = "Al-Malaz, Riyadh", notes = "Purchased accessories and repairs"),
            Customer(name = "Usman Farooq", phone = "+966 54 332 1199", address = "Al-Sulaimaniyah, Riyadh", notes = "Frequent mobile buyer")
        )
        dao.insertCustomersBulk(customers)

        // Expenses
        val expenses = listOf(
            Expense(name = "Shop Electricity Bill", amount = 320.0, category = "Electricity & Bills", date = now - 86400000L * 2, description = "Monthly power bill"),
            Expense(name = "Staff Tea & Refreshments", amount = 35.0, category = "Tea / Food / Staff", date = now, description = "Daily refreshment"),
            Expense(name = "Soldering Flux & Screen Glue", amount = 70.0, category = "Repair Parts / Tools", date = now - 86400000L, description = "Consumable repair supplies")
        )
        dao.insertExpensesBulk(expenses)
    }
}
