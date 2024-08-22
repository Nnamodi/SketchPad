package dev.borisochieng.sketchpad.ui.screens.drawingboard.chat

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.borisochieng.sketchpad.database.repository.TAG
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel

@Composable
fun ChatDialog(
    boardId: String,
    viewModel: SketchPadViewModel,
    onCancel: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val typingUsers by viewModel.typingUsers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load(boardId)
    }
    LaunchedEffect(Unit) {
        viewModel.listenForTypingStatuses(boardId)
    }

    Dialog(onDismissRequest = onCancel) {
        val messages by viewModel.messages.collectAsState()
        Log.d(TAG, "list of messages in chatsScreen $messages")

        Card {
            Column(Modifier.padding(16.dp, 8.dp)) {
                Text(
                    text = "Chat",
                    fontSize = 24.sp,
                    fontWeight = FontWeight(400)
                )
                if (typingUsers.isNotEmpty()) {
                    Text(
                        text = "${typingUsers[0]} is typing...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400)
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                items(messages.size) { index ->
                    val checkNextSame = checkNextSame(index, messages)
                    val viewType = getItemViewType(index, messages)

                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val senderCount = remember { mutableStateOf(false) }
                        val receiverCount = remember { mutableStateOf(false) }

                        if (viewType == SENDER_VIEW_TYPE) {
                            if (checkNextSame == true) {
                                senderCount.value = true
                            }
                            SenderChat(
                                message = messages[index]!!.message,
                                maxWidth = maxWidth,
                                backgroundColor = Color(0xFF1EBE71),
                                textColor = Color.White,
                                senderCount = senderCount,
                                time = messages[index]!!.timestamp!!,
                                senderName = messages[index]?.senderName ?: ""
                            )
                            senderCount.value = false
                        } else {
                            if (checkNextSame == true) {
                                receiverCount.value = true
                            }
                            ReceiverChat(
                                message = messages[index]!!.message,
                                maxWidth = maxWidth,
                                backgroundColor = Color(0xFFF2F2F2),
                                textColor = Color(0xFF000000),
                                receiverCount = receiverCount,
                                time = messages[index]!!.timestamp!!,
                                receiverName = messages[index]?.senderName ?: ""
                            )
                            receiverCount.value = false
                        }
                    }
                }
            }

            // A horizontal divider to separate the content and the footer
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier
					.fillMaxWidth()
                    .padding(bottom = 4.dp),
                thickness = 1.dp
            )
            ChatEditText(
                text = viewModel.messageState.value.message,
                onValueChange = { text ->
                    viewModel.onMessageChange(text, boardId)
                },
                onSendActionClicked = {
                    viewModel.onMessageSent(boardId)
                    keyboardController?.hide()
                    viewModel.messageState.value =
                        viewModel.messageState.value.copy(message = "")
                },
                viewModel = viewModel,
                projectId = boardId
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}