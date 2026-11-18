package com.storemanager.afada.ui.report

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

data class VietnamBankInfo(
    val shortName: String,
    val fullName: String,
    val bin: String
)

/**
 * Bộ công cụ chuẩn NAPAS 247 VietQR (EMVCo QR Code):
 * 1. Danh sách các ngân hàng phổ biến tại Việt Nam kèm mã BIN 6 số chuẩn NAPAS.
 * 2. Hàm sinh chuỗi EMVCo VietQR chuẩn xác (kèm mã kiểm tra CRC16-CCITT) để 100% App Ngân hàng
 *    (Vietcombank, MB, Techcombank, BIDV, VietinBank, Agribank, ACB, MoMo, ZaloPay...) quét ra đúng
 *    Ngân hàng, Số tài khoản, Tên chủ tài khoản và Số tiền!
 * 3. Hỗ trợ sinh Bitmap QR Offline sắc nét bằng ZXing (không cần mạng vẫn tạo và in được QR).
 */
object VietQrGenerator {

    val POPULAR_BANKS: List<VietnamBankInfo> = listOf(
        VietnamBankInfo("Vietcombank", "NH TMCP Ngoại Thương Việt Nam (VCB)", "970436"),
        VietnamBankInfo("MBBank", "NH TMCP Quân Đội (MB)", "970422"),
        VietnamBankInfo("Techcombank", "NH TMCP Kỹ Thương Việt Nam (TCB)", "970407"),
        VietnamBankInfo("VietinBank", "NH TMCP Công Thương Việt Nam (CTG)", "970415"),
        VietnamBankInfo("BIDV", "NH TMCP Đầu Tư và Phát Triển VN", "970418"),
        VietnamBankInfo("Agribank", "NH Nông Nghiệp & PTNT Việt Nam", "970405"),
        VietnamBankInfo("ACB", "NH TMCP Á Châu", "970416"),
        VietnamBankInfo("VPBank", "NH TMCP Việt Nam Thịnh Vượng", "970432"),
        VietnamBankInfo("TPBank", "NH TMCP Tiên Phong", "970423"),
        VietnamBankInfo("Sacombank", "NH TMCP Sài Gòn Thương Tín", "970403"),
        VietnamBankInfo("HDBank", "NH TMCP Phát Triển TP.HCM", "970437"),
        VietnamBankInfo("VIB", "NH TMCP Quốc Tế Việt Nam", "970441"),
        VietnamBankInfo("SHB", "NH TMCP Sài Gòn - Hà Nội", "970443"),
        VietnamBankInfo("MSB", "NH TMCP Hàng Hải Việt Nam", "970426"),
        VietnamBankInfo("OCB", "NH TMCP Phương Đông", "970448"),
        VietnamBankInfo("SeABank", "NH TMCP Đông Nam Á", "970440"),
        VietnamBankInfo("LPBank", "NH TMCP Lộc Phát Việt Nam", "970449"),
        VietnamBankInfo("Eximbank", "NH TMCP Xuất Nhập Khẩu VN", "970431")
    )

    private fun tlv(id: String, value: String): String {
        val len = value.length.toString().padStart(2, '0')
        return "$id$len$value"
    }

    /**
     * Tạo chuỗi EMVCo VietQR chuẩn NAPAS 247 (Quét được bằng mọi ứng dụng ngân hàng tại Việt Nam)
     * - Nếu `amount` != null và > 0: Tạo mã QR động kèm sẵn số tiền thanh toán.
     * - Nếu `amount` == null hoặc <= 0: Tạo mã QR tĩnh chỉ quét ra Ngân hàng + Số tài khoản.
     */
    fun buildEmvCoVietQrPayload(
        bankBin: String,
        accountNumber: String,
        amount: Long? = null,
        description: String? = null
    ): String {
        val cleanBin = bankBin.trim().ifBlank { "970436" }
        val cleanAcc = accountNumber.trim().ifBlank { "0123456789" }

        val isDynamic = amount != null && amount > 0L

        // Tag 00: Payload Format Indicator = "01"
        val p00 = tlv("00", "01")
        // Tag 01: Point of Initiation Method ("11" = Static QR, "12" = Dynamic QR with amount)
        val p01 = tlv("01", if (isDynamic) "12" else "11")

        // Tag 38: Merchant Account Information (NAPAS VietQR GUID = A000000727)
        val beneficiary = tlv("00", cleanBin) + tlv("01", cleanAcc)
        val merchantAccount = tlv("00", "A000000727") +
            tlv("01", beneficiary) +
            tlv("02", "QRIBFTTA")
        val p38 = tlv("38", merchantAccount)

        // Tag 53: Transaction Currency (VND = "704")
        val p53 = tlv("53", "704")

        // Tag 54: Transaction Amount (nếu chọn chế độ QR theo số tiền trên hóa đơn)
        val p54 = if (isDynamic) tlv("54", amount.toString()) else ""

        // Tag 58: Country Code = "VN"
        val p58 = tlv("58", "VN")

        // Tag 62: Additional Data Field Template (Nội dung chuyển khoản)
        val p62 = if (!description.isNullOrBlank()) {
            tlv("62", tlv("08", description.trim()))
        } else ""

        // Tag 63: CRC16-CCITT (4 ký tự Hex viết hoa)
        val rawBeforeCrc = "$p00$p01$p38$p53$p54$p58${p62}6304"
        val crcHex = computeCrc16Ccitt(rawBeforeCrc)
        return rawBeforeCrc + crcHex
    }

    private fun computeCrc16Ccitt(input: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021
        val bytes = input.toByteArray(Charsets.UTF_8)
        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = ((b.toInt() shr (7 - i)) and 1) == 1
                val c15 = ((crc shr 15) and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) {
                    crc = crc xor polynomial
                }
            }
        }
        crc = crc and 0xFFFF
        return crc.toString(16).uppercase().padStart(4, '0')
    }

    /**
     * Tạo Bitmap mã QR đen trắng sắc nét từ chuỗi EMVCo VietQR bằng thư viện ZXing
     */
    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
                }
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (_: Exception) {
            null
        }
    }
}
