package com.storemanager.afada.data.local

/**
 * Dữ liệu mẫu chuẩn ngành Bia - Rượu - Nước Ngọt tại Việt Nam
 * Mỗi mặt hàng được cấu hình danh sách đơn vị Két / Thùng / Lốc:
 * - Đơn vị nào có giá bán > 0 sẽ tự động hiện thành 1 mặt hàng riêng khi tìm kiếm/bán hàng ở tab Bán hàng!
 */
object SeedData {

    fun sampleProducts(dbId: String): List<ProductEntity> = listOf(
        ProductEntity("${dbId}_SP00", dbId, "Bia Hà Nội Lon", "Bia", "Lon", 0.0, 12000.0, 480, 72, "preset:beer_gold"),
        ProductEntity("${dbId}_SP01", dbId, "Bia Tiger Nâu 330ml", "Bia", "Lon", 0.0, 16500.0, 480, 72, "preset:beer_blue"),
        ProductEntity("${dbId}_SP02", dbId, "Bia Tiger Bạc 330ml", "Bia", "Lon", 0.0, 18000.0, 360, 72, "preset:beer_silver"),
        ProductEntity("${dbId}_SP03", dbId, "Bia Heineken Sleek", "Bia", "Lon", 0.0, 21000.0, 288, 48, "preset:beer_green"),
        ProductEntity("${dbId}_SP04", dbId, "Bia Sài Gòn Lager", "Bia", "Lon", 0.0, 13000.0, 600, 96, "preset:beer_red"),
        ProductEntity("${dbId}_SP05", dbId, "Bia Sài Gòn Đỏ Chai", "Bia", "Chai", 0.0, 12000.0, 40, 60, "preset:crate_red"),
        ProductEntity("${dbId}_SP06", dbId, "Coca-Cola 320ml", "Nước ngọt", "Lon", 0.0, 10000.0, 432, 72, "preset:soda_red"),
        ProductEntity("${dbId}_SP07", dbId, "Pepsi 320ml", "Nước ngọt", "Lon", 0.0, 10000.0, 360, 72, "preset:soda_blue"),
        ProductEntity("${dbId}_SP08", dbId, "Bò Húc RedBull", "Nước ngọt", "Lon", 0.0, 13000.0, 240, 48, "preset:energy_gold"),
        ProductEntity("${dbId}_SP09", dbId, "Nước Suối Aquafina", "Nước suối", "Chai", 0.0, 5000.0, 560, 84, "preset:water_cyan"),
        ProductEntity("${dbId}_SP10", dbId, "Rượu Vodka Men", "Rượu", "Chai", 0.0, 80000.0, 48, 12, "preset:wine_purple")
    )

    fun sampleProductUnits(dbId: String): List<ProductUnitEntity> = listOf(
        // SP00: Bia Hà Nội Lon (Ví dụ mẫu: Nhập giá Thùng = 250.000, giá Lốc = 65.000, không nhập giá Két)
        ProductUnitEntity("${dbId}_U00_THUNG", dbId, "${dbId}_SP00", "Thùng", 24, 250000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U00_LOC", dbId, "${dbId}_SP00", "Lốc", 6, 65000.0, 0.0, "", false),

        // SP01: Tiger Nâu (Thùng + Lốc)
        ProductUnitEntity("${dbId}_U01_THUNG", dbId, "${dbId}_SP01", "Thùng", 24, 355000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U01_LOC", dbId, "${dbId}_SP01", "Lốc", 6, 92000.0, 0.0, "", false),

        // SP02: Tiger Bạc (Thùng + Lốc)
        ProductUnitEntity("${dbId}_U02_THUNG", dbId, "${dbId}_SP02", "Thùng", 24, 385000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U02_LOC", dbId, "${dbId}_SP02", "Lốc", 6, 99000.0, 0.0, "", false),

        // SP03: Heineken (Thùng + Lốc)
        ProductUnitEntity("${dbId}_U03_THUNG", dbId, "${dbId}_SP03", "Thùng", 24, 455000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U03_LOC", dbId, "${dbId}_SP03", "Lốc", 6, 118000.0, 0.0, "", false),

        // SP04: Sài Gòn Lager (Thùng + Lốc)
        ProductUnitEntity("${dbId}_U04_THUNG", dbId, "${dbId}_SP04", "Thùng", 24, 275000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U04_LOC", dbId, "${dbId}_SP04", "Lốc", 6, 72000.0, 0.0, "", false),

        // SP05: Sài Gòn Đỏ Chai (Chỉ bán theo Két)
        ProductUnitEntity("${dbId}_U05_KET", dbId, "${dbId}_SP05", "Két", 20, 215000.0, 0.0, "", true),

        // SP06: Coca-Cola (Thùng + Lốc)
        ProductUnitEntity("${dbId}_U06_THUNG", dbId, "${dbId}_SP06", "Thùng", 24, 195000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U06_LOC", dbId, "${dbId}_SP06", "Lốc", 6, 52000.0, 0.0, "", false),

        // SP07: Pepsi (Thùng + Lốc)
        ProductUnitEntity("${dbId}_U07_THUNG", dbId, "${dbId}_SP07", "Thùng", 24, 192000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U07_LOC", dbId, "${dbId}_SP07", "Lốc", 6, 51000.0, 0.0, "", false),

        // SP08: RedBull (Thùng + Lốc)
        ProductUnitEntity("${dbId}_U08_THUNG", dbId, "${dbId}_SP08", "Thùng", 24, 260000.0, 0.0, "", true),
        ProductUnitEntity("${dbId}_U08_LOC", dbId, "${dbId}_SP08", "Lốc", 6, 68000.0, 0.0, "", false),

        // SP09: Aquafina (Thùng)
        ProductUnitEntity("${dbId}_U09_THUNG", dbId, "${dbId}_SP09", "Thùng", 28, 110000.0, 0.0, "", true),

        // SP10: Rượu Vodka (Thùng)
        ProductUnitEntity("${dbId}_U10_THUNG", dbId, "${dbId}_SP10", "Thùng", 12, 900000.0, 0.0, "", true)
    )

    fun sampleCustomers(dbId: String): List<CustomerEntity> = listOf(
        CustomerEntity("${dbId}_KH_LE", dbId, "Khách lẻ", "", "Mua tại quầy", 0.0, ""),
        CustomerEntity("${dbId}_KH01", dbId, "Quán Hải Sản Biển Đông", "0909123456", "Quận 7", 2450000.0, "Nợ 8 vỏ két Sài Gòn Đỏ"),
        CustomerEntity("${dbId}_KH02", dbId, "Tạp Hóa Cô Ba", "0918888999", "Thủ Đức", 850000.0, "Nợ 3 vỏ két Sài Gòn"),
        CustomerEntity("${dbId}_KH03", dbId, "Nhà Hàng Hoàng Gia", "0987654321", "Tân Bình", 5200000.0, "Giữ 15 vỏ két bia")
    )
}
