package com.example.dailytaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random
import androidx.compose.material3.Card
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults


data class Task(
    val id: Int = Random.nextInt(),
    val title: String,
    val hour: Int,
    val minute: Int,
    val date: LocalDate,
    var isCompleted: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskManagerApp()
        }
    }
}

@Preview
@Composable
fun TaskManagerApp() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val tasks = remember { mutableStateListOf<Task>() }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    fun isOverdue(task: Task): Boolean {
        val now = LocalDateTime.now()
        val taskTime = task.date.atTime(task.hour, task.minute)
        return now.isAfter(taskTime)
    }

    val ongoingTasks = tasks.filter {
        it.date == selectedDate && !it.isCompleted && !isOverdue(it)
    }
    val completedTasks = tasks.filter {
        it.date == selectedDate && it.isCompleted == true
    }
    val overdueTasks = tasks.filter {
        it.date == selectedDate && !it.isCompleted && isOverdue(it)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color.White,         // Background color
                contentColor = Color.Black            // Icon color
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White) // Makes the background white
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(4.dp), // Optional padding around the card
                colors = CardDefaults.cardColors(
                    containerColor = Color.White // Set background color to white
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp // Adds shadow
                )

            ) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp), // Optional padding inside the card

                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Today", fontSize = 40.sp,  fontWeight = FontWeight.Bold)

                    Text(" ${selectedDate}", fontSize = 20.sp)
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick Date",
                            modifier = Modifier.size(60.dp) // Adjust the size as needed
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))


            TaskListSection("Ongoing", ongoingTasks,
                onComplete = { task ->
                    // Mark task as completed and trigger recomposition
                    task.isCompleted = true
                    tasks.remove(task) // Re-add to reflect updated task in correct section
                    tasks.add(task)
                },
                onDelete = { tasks.remove(it) })



            Spacer(modifier = Modifier.height(8.dp))


            TaskListSection("Completed", completedTasks)


            Spacer(modifier = Modifier.height(8.dp))


            TaskListSection("Overdue", overdueTasks)




            if (showDialog) {
                AddTaskDialog(
                    onAdd = { title, hour, minute ->
                        tasks.add(Task(title = title, hour = hour, minute = minute, date = selectedDate))
                        showDialog = false
                    },
                    onCancel = { showDialog = false }
                )
            }
        }
    }
}


@Composable
fun TaskListSection(
    title: String,
    tasks: List<Task>,
    onComplete: ((Task) -> Unit)? = null,
    onDelete: ((Task) -> Unit)? = null
) {

    Column(){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp) // Optional padding for the Row
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, size.height), // Start from the left bottom of the Row
                        end = Offset(size.width, size.height), // End at the right bottom of the Row
                        strokeWidth = strokeWidth
                    )
                }
        ){
            Text("$title (${tasks.size})", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp),  fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(tasks) { task ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF9FAFB) // Set background color to white
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp // Adds shadow
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row (
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(String.format("%02d:%02d", task.hour, task.minute),
                                color = when {
                                    task.isCompleted -> Color(0xFF00DE07) // Green: #00DE07
                                    title == "Overdue" -> Color(0xFFCB0000) // Red: #CB0000
                                    else -> Color(0xFF1CD5FF) // Blue: #1CD5FF
                                }, fontSize = 16.sp)

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(task.title, fontSize = 16.sp)
                        }

                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (onComplete != null) {
                                IconButton(onClick = { onComplete(task) }) {
                                    Icon(Icons.Default.Check, contentDescription = "Complete")
                                }
                            }
                            if (onDelete != null) {
                                IconButton(onClick = { onDelete(task) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun AddTaskDialog(onAdd: (String, Int, Int) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = Color.White, // Sets dialog background to white
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = {
                    val h = hour.toIntOrNull() ?: 0
                    val m = minute.toIntOrNull() ?: 0
                    onAdd(title, h, m)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White // sets the text color
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        },
        title = { Text("Add New Task", color = Color.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task", color = Color.Black) },
                    colors = OutlinedTextFieldDefaults.colors(Color.Black)
                )
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text("Hour", color = Color.Black) },
                    colors = OutlinedTextFieldDefaults.colors(Color.Black)
                )
                OutlinedTextField(
                    value = minute,
                    onValueChange = { minute = it },
                    label = { Text("Minute", color = Color.Black) },
                    colors = OutlinedTextFieldDefaults.colors(Color.Black)
                )
            }
        }
    )

}