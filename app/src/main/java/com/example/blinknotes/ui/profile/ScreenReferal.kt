package com.example.blinknotes.ui.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blinknotes.R


@Composable
fun ListReferalScreen(){

    Scaffold (
        topBar = {
            Header()
        }
    ) { paddingValues ->

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues = paddingValues)
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(color = colorResource(R.color.bgr))
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, end = 16.dp, start = 16.dp, bottom = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ItemsDate(datedata = null, text = "Từ", modifier = Modifier.weight(1f))
                    // Spacer(modifier = Modifier.weight(1f))
                    ItemsDate(datedata = null, text = "Đến", modifier = Modifier.weight(1f))

                }
                Text(
                    text = "5 người bạn",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = colorResource(R.color.text_my_qr)
                )
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(
                        listOf(
                            Triple(R.drawable.logo_app, "Lê Nhựt Trường", "0785790205"),
                            Triple(R.drawable.logo_app, "Nguyễn Văn A", "0901234567"),
                            Triple(R.drawable.logo_app, "Trần Thị B", "0917654321")
                        )
                    ) { (icon, name, phone) ->
                        ItemsList(icon = icon, name = name, numPhone = phone)
                        Spacer(modifier = Modifier.size(12.dp)) // Thêm khoảng cách giữa các items
                    }
                }

            }
        }
    }
}
@Composable
fun ItemsDate(
    datedata: String?,
    text: String,
    modifier: Modifier = Modifier
){
    val textToShow = remember { mutableStateOf(text) }

    LaunchedEffect(datedata) {
        if (!datedata.isNullOrEmpty()) {
            textToShow.value = datedata
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colorResource(R.color.white), shape = RoundedCornerShape(100.dp)),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = textToShow.value,
            fontSize = 14.sp,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
            textAlign = TextAlign.Left
        )

        Image(
            painter = painterResource(R.drawable.calendar),
            contentDescription = "",
            modifier = Modifier
                .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            alignment = Alignment.CenterEnd
        )
    }

}
@Composable
fun Header(){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            painter = painterResource(R.drawable.icon_back),
            modifier = Modifier
                .size(24.dp),
            contentDescription = "",
            alignment = Alignment.Center
        )
        Text(
            text = "Danh sách đã giới thiệu",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.black),
            modifier = Modifier
                .padding(start = 16.dp)
        )
    }
}

@Composable
fun ItemsList(
    @DrawableRes icon: Int,
    name: String,
    numPhone: String ,
){
    val formatNumPhone = isValidPhoneNumber(numPhone)
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically

    ){
        Image(
            painter = painterResource(id = icon),
            contentDescription = "avt",
            modifier = Modifier
                .size(40.dp)
               // .padding(end = 16.dp)
                .clip(shape = CircleShape),
            contentScale = ContentScale.Crop
        )
        Column (
            modifier = Modifier.
            padding(start = 16.dp)
        ){
            Text(
                text = name,
                fontSize = 14.sp,
                color = colorResource(R.color.black),
                maxLines = 1

            )
            Text(
                text = formatNumPhone,
                fontSize = 14.sp,
                color = colorResource(R.color.text_my_qr),
                maxLines = 1

            )
        }
    }
}
private fun isValidPhoneNumber(phoneNumber: String): String{
    return if (phoneNumber.length >= 7) {
        phoneNumber.take(4) + "***" + phoneNumber.takeLast(3)
    } else {
        "Số không hợp lệ"
    }
}
//@Preview(showBackground = true)
//@Composable
//fun Preview1(){
//    //ItemsList(icon = R.drawable.logo_app, name = "Lê Nhựt Trường", numPhone = "0785790205")
//// ItemsDate(datedata = null, text = "tu", modifier = Modifier)
//    ListReferalScreen()
//}
