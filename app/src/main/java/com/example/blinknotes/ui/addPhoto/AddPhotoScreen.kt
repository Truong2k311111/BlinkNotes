package com.example.blinknotes.ui.addPhoto


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.IconButton
import coil.compose.rememberAsyncImagePainter
import com.example.blinknotes.R
import com.example.blinknotes.ui.home.LoadingAnimation
@Composable
fun AddPhotoScreen(navController: NavHostController, viewModel: AddPhotoScreenViewModel = viewModel()) {
    var caption by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    var showVisibilitySheet by remember { mutableStateOf(false) }
    var selectedVisibility by remember { mutableStateOf("public") }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            viewModel.addSelectedImages(uris)
        }
    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
            modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                    Row(
                        modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        modifier = Modifier,
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_back),
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier
                        )
                    }
                }
                    Row(
                        modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = colorResource(R.color.white))
                                .clickable { },
                        ) {
                        Text(
                            text = "Bản nháp",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(alignment = Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = colorResource(R.color.azure))
                            .clickable {
                                    if (selectedImages.isNotEmpty()) {
                                        viewModel.uploadImagesToFirebase(
                                            caption = caption,
                                            content = content,
                                            visibility = selectedVisibility,
                                            context = context,
                                        ) {
                                            navController.popBackStack()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Vui lòng chọn ảnh!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        ) {
                        Text(
                            text = "Đăng",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(alignment = Alignment.Center)
                        )
                        }
                    }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colorResource(R.color.darkolivegreen)),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingAnimation()
                }
            }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(70.dp)
                        .fillMaxWidth(),
                    color = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Column {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(color = colorResource(R.color.bgr))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = null
                                ) {
                                    showVisibilitySheet = true
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = if (selectedVisibility == "public")
                                    painterResource(R.drawable.earth)
                                else
                                    painterResource(R.drawable.lock),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedVisibility == "public")
                                    "Mọi người có thể xem và bình luận"
                                else
                                    "Chỉ mình tôi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = colorResource(R.color.gainsboro),
                                        shape = RoundedCornerShape(size = 12.dp)
                                    )
                                    .size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.eye_outline),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                    .padding(paddingValues = paddingValues)
                    .padding(bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .padding(8.dp)
            ) {
                items(selectedImages) { uri ->
                    ItemsImage(
                        painter = rememberAsyncImagePainter(uri),
                            onEdit = { },
                        onDelete = {
                            viewModel.updateSelectedImages(selectedImages - uri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                                .aspectRatio(2.5f / 3.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .clip(RoundedCornerShape(10.dp))
                                .aspectRatio(2.5f / 3.5f)
                                .background(colorResource(R.color.beige))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "Add Photo",
                                tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
                Spacer(modifier = Modifier.height(8.dp))
                Content(caption = caption, content = content, onCaptionChange = { caption = it },
                onContentChange = { content = it })
        }
    }

        if (showVisibilitySheet) {
            VisibilityBottomSheet(
                showBottomSheet = true,
                onDismiss = { showVisibilitySheet = false },
                onVisibilitySelected = { visibility ->
                    selectedVisibility = visibility
                    showVisibilitySheet = false
                },
                currentVisibility = selectedVisibility,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
@Composable
fun Content(
    caption: String,
    content: String,
    onCaptionChange: (String) -> Unit,
    onContentChange: (String) -> Unit
){

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        CustomTextFieldContent(
            value = caption,
            keyboardType = KeyboardType.Text,
            placeholder = "Thêm tiêu đề hấp dẫn",
            onValueChange = onCaptionChange,
            modifier = Modifier,
            set = "caption",
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

        )
        Divider(modifier = Modifier.fillMaxWidth(0.9f).align(alignment = Alignment.CenterHorizontally))
        CustomTextFieldContent(
            value = content,
            keyboardType = KeyboardType.Text,
            placeholder = "Cho dù ảnh của bạn có chủ đề về du lịch, thể dục, nấu ăn hay các sở thích khác, hãy thêm mô tả hữu ích để giúp người khác tìm hiểu thêm",
            onValueChange = onContentChange,
            set = "content",
            textStyle = TextStyle(
                fontSize = 20.sp,
            )
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextFieldContent(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    set: String,
    textStyle: TextStyle
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            if(set == "caption"){
            Text(
                text = placeholder,
                modifier = modifier,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.bgr),
                fontSize = 20.sp
            )
            } else {
                Text(
                    text = placeholder,
                    modifier = modifier,
                    color = colorResource(R.color.bgr),
                    fontSize = 18.sp
                )
            }

                                },
        visualTransformation = VisualTransformation.None,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth(),
        textStyle = textStyle
    )
}

@Composable
fun ItemsImage(
    painter: Painter = rememberAsyncImagePainter(null),
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(12.dp))
            .background(Color.Gray)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        IconButton(
            onClick = { onDelete() },
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Xóa ảnh",
                tint = Color.White,
                modifier = Modifier
                    .size(26.dp)
            )
        }
        IconButton(
            onClick = { onEdit() },
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Chỉnh sửa ảnh",
                tint = Color.White,
                modifier = Modifier
                    .size(26.dp)
            )
        }
    }
}

