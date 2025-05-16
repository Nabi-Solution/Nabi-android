package com.gdghufs.nabi.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily
@Composable
@Preview(showBackground = true)
fun TimeTagsPreview() {
    NabiTheme { // Assuming you have this Theme wrapper
        Column {
            TimeTag(TimeTagType.AnyTime)
            TimeTag(TimeTagType.Afternoon)
            TimeTag(TimeTagType.Morning)
            TimeTag(TimeTagType.Evening)
        }
    }
}

@Composable
fun TimeTag(
    type: TimeTagType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(type.backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(6.dp, 4.dp)
    ) {
        Text(
            text = type.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, // Adjusted
            fontFamily = RobotoPretendardFamily,   // Adjusted
            color = type.textColor,
        )
    }
}

sealed class TimeTagType {
    data object AnyTime : TimeTagType()
    data object Afternoon : TimeTagType()
    data object Morning : TimeTagType()
    data object Evening : TimeTagType()

    val text: String
        get() = when (this) {
            is AnyTime -> "ANYTIME"
            is Afternoon -> "AFTERNOON"
            is Morning -> "MORNING"
            is Evening -> "EVENING"
        }

    val textColor: Color
        get() = when (this) {
            is AnyTime -> Color(0xff24C2F2)
            is Afternoon -> Color(0xff46CF8B)
            is Morning -> Color(0xff7990F8)
            is Evening -> Color(0xffBC5EAD)
        }

    val backgroundColor: Color
        get() = when (this) {
            is AnyTime -> Color(0x1A00F2FF)
            is Afternoon -> Color(0x1A46CF8B)
            is Morning -> Color(0x1A7990F8)
            is Evening -> Color(0x1ABC5EAD)
        }
}