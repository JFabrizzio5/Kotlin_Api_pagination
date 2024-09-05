package com.example.exampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.exampleapp.ui.theme.ExampleappTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExampleappTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PaginatedPosts()
                }
            }
        }
    }
}

@Composable
fun PaginatedPosts() {
    var posts by remember { mutableStateOf(emptyList<Post>()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasMorePosts by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) } // Inicialmente en 1
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentPage) {
        if (currentPage > 0) { // Asegúrate de que la página actual sea válida
            isLoading = true
            coroutineScope.launch {
                try {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://jsonplaceholder.typicode.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val api = retrofit.create(JsonPlaceholderApi::class.java)
                    val response = api.getPosts(page = currentPage, limit = 10)

                    if (response.isSuccessful) {
                        val newPosts = response.body() ?: emptyList()
                        posts = newPosts
                        hasMorePosts = newPosts.size == 10 // Asume que 10 elementos es el tamaño de página

                        // Aquí ajustarías el total de páginas si la API lo devuelve
                        // Por ejemplo, si la API te proporciona un total de registros:
                        val totalRecords = response.headers()["X-Total-Count"]?.toInt() ?: 0
                        totalPages = (totalRecords + 9) / 10 // Calcula el total de páginas

                        // Ocultar el botón "Next" si no hay más posts
                        if (newPosts.size < 10 || currentPage >= totalPages) {
                            hasMorePosts = false
                        }
                    } else {
                        // Manejar error de respuesta
                        hasMorePosts = false
                    }
                } catch (e: Exception) {
                    // Manejar error de red
                    hasMorePosts = false
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Encabezado de la tabla
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TableHeader("UserId", "ID", "Title", "Completed")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Caja con scroll vertical para los resultados de la tabla
        if (posts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f) // Ocupa el espacio restante
                    .fillMaxWidth()
                    .height(400.dp) // Altura fija para la tabla
                    .border(1.dp, Color.Gray) // Borde para la tabla
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()) // Scroll vertical dentro de la caja
                ) {
                    posts.forEach { post ->
                        TableRow(post.userId, post.id, post.title, post.completed)
                    }
                }
            }
        } else if (!isLoading) {
            // Mensaje cuando no hay más posts para mostrar
            Text(
                text = "No more posts available.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
        }

        // Indicador de páginas
        Text(
            text = "Page $currentPage of $totalPages",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de navegación
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { if (currentPage > 1) currentPage-- },
                enabled = currentPage > 1
            ) {
                Text("Previous")
            }

            Button(
                onClick = { if (currentPage < totalPages) currentPage++ },
                enabled = currentPage < totalPages
            ) {
                Text("Next")
            }
        }
    }
}



@Composable
fun TableHeader(userIdHeader: String, idHeader: String, titleHeader: String, completedHeader: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(userIdHeader, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(idHeader, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(titleHeader, modifier = Modifier.weight(3f), textAlign = TextAlign.Center)
        Text(completedHeader, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
}

@Composable
fun TableRow(userId: Int, id: Int, title: String, completed: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(userId.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(id.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(title, modifier = Modifier.weight(3f), textAlign = TextAlign.Center)
        Text(completed.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
}

interface JsonPlaceholderApi {
    @GET("todos")
    suspend fun getPosts(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int
    ): retrofit2.Response<List<Post>>
}

data class Post(val userId: Int, val id: Int, val title: String, val completed: Boolean)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Este es la vista de nuevo $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPaginatedPosts() {
    ExampleappTheme {
        PaginatedPosts()
    }
}
