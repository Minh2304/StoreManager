package com.storemanager.afada.core.designsystem

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaBlueSecondary
import java.io.File

/**
 * Thanh Header màu xanh dương đậm chuẩn thương hiệu AFaDa 4.0
 * Có hỗ trợ nút Back (onBack) để quay về Trang chủ khi đã bỏ thanh điều hướng dưới cùng
 */
@Composable
fun AfadaTopHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    rightAction: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(AfadaBluePrimary, AfadaBlueSecondary)
                )
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (rightAction != null) {
                Spacer(modifier = Modifier.width(8.dp))
                rightAction()
            }
        }
    }
}

/**
 * Ô hiển thị Ảnh sản phẩm tỷ lệ vuông 1:1
 * - Hỗ trợ đọc ảnh đã crop lưu trong bộ nhớ máy (file://, đường dẫn tuyệt đối) hoặc content://
 * - Hỗ trợ hiển thị biểu tượng bút chì mờ (`showEditOverlay = true`) để người dùng chạm vào đổi/crop ảnh
 */
@Composable
fun AfadaProductThumbnail(
    imageUri: String,
    category: String = "",
    size: Dp = 96.dp,
    cornerRadius: Dp = 14.dp,
    showEditOverlay: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val loadedBitmap = remember(imageUri) {
        when {
            imageUri.startsWith("content://") || imageUri.startsWith("file://") -> {
                try {
                    context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } catch (_: Exception) {
                    null
                }
            }
            imageUri.startsWith("/") -> {
                try {
                    val file = File(imageUri)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
                } catch (_: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    val (bgColor, iconTint, iconVector) = when {
        imageUri == "preset:beer_gold" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.SportsBar)
        imageUri == "preset:beer_green" -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), Icons.Default.SportsBar)
        imageUri == "preset:beer_red" || imageUri == "preset:crate_red" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.SportsBar)
        imageUri == "preset:soda_red" -> Triple(Color(0xFFFCE4EC), Color(0xFFD81B60), Icons.Default.LocalDrink)
        imageUri == "preset:soda_blue" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.LocalDrink)
        imageUri == "preset:water_cyan" -> Triple(Color(0xFFE0F7FA), Color(0xFF00838F), Icons.Default.WaterDrop)
        imageUri == "preset:wine_purple" -> Triple(Color(0xFFF3E5F5), Color(0xFF6A1B9A), Icons.Default.LocalBar)
        else -> Triple(Color(0xFFFFF8E1), Color(0xFFF57C00), Icons.Default.SportsBar)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (loadedBitmap != null) {
            Image(
                bitmap = loadedBitmap,
                contentDescription = "Ảnh món hàng",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(size * 0.50f)
            )
        }

        // Biểu tượng bút chì mờ để báo hiệu có thể chạm vào sửa ảnh
        if (showEditOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.38f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Đổi ảnh",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Ô biểu tượng tròn chuẩn lưới menu AFaDa
 */
@Composable
fun AfadaMenuIconTile(
    title: String,
    icon: ImageVector,
    color: Color,
    badgeText: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
            }

            if (!badgeText.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFD32F2F),
                    shape = CircleShape,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Thẻ thống kê số liệu chữ to chuẩn AFaDa
 */
@Composable
fun AfadaStatBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        }
    }
}
