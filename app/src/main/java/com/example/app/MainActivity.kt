package com.example.app
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.db.FeedReaderDbHelper
import com.example.app.db.Product
import com.example.app.ui.theme.AppTheme
import java.io.File


// Trendy colors with gradient effects
val CandyPink = Color(0xFFFF69B4)
val SkyBlue = Color(0xFF87CEEB)
val GradientWhite = Color(0xFFFFFFFF)
private const val DATABASE_NAME = "UserData.db"
private const val DATABASE_VERSION = 1

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ProductOperations()
            }
        }
    }
}
@Composable
fun ProductOperations() {
    val dbHelper = FeedReaderDbHelper(LocalContext.current)
    val db = dbHelper.writableDatabase

    // Insertion d'un produit
    val productToInsert = Product("Produit 1", "Description du produit", painterResource(id = R.drawable.img1))
    val productId = dbHelper.insertProduct(db, productToInsert)

    // Lecture d'un produit
    val product = dbHelper.readProduct(db, productId)
    product?.let {
        // Utilisez un toast ou une autre méthode pour afficher le produit
        Toast.makeText(LocalContext.current, "Nom: ${it.name}, Description: ${it.description}", Toast.LENGTH_SHORT).show()
    }

    // Mise à jour d'un produit
    val updatedProduct = Product("Produit 1 Modifié", "Description mise à jour", painterResource(id = R.drawable.img2))
    dbHelper.updateProduct(db, productId, updatedProduct)

    // Suppression d'un produit
    dbHelper.deleteProduct(db, productId)
}

class UserDataDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE User (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT,
                password TEXT
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS User")
        onCreate(db)
    }

    fun insertUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val insertQuery = "INSERT INTO User (name, email, password) VALUES (?, ?, ?)"
        val statement = db.compileStatement(insertQuery)
        statement.bindString(1, name)
        statement.bindString(2, email)
        statement.bindString(3, password)

        return statement.executeInsert() != -1L
    }

    fun fetchAllUsers(): List<User> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM User", null)
        val users = mutableListOf<User>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
                val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                users.add(User(id, name, email, password))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return users
    }
}

// User data class
data class User(val id: Int, val name: String, val email: String, val password: String)

fun saveUserData(context: Context,name: String, email: String, password: String) {
    val userData = "Name: $name\nEmail: $email\nPassword: $password"
    val file = File(context.filesDir, "userData.txt")
    file.writeText(userData)
}
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("Login") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CandyPink, SkyBlue, GradientWhite) // Trendy background gradient
                )
            )
    ) {
        when (currentScreen) {
            "Login" -> LoginScreen(
                onLoginSuccess = { currentScreen = "Home" },
                onForgotPasswordClick = { currentScreen = "ResetPassword" },
                onSignUpClick = { currentScreen = "SignUp" }
            )
            "Home" -> HomeScreen(onLogoutClick = { currentScreen = "Login" })
            "ResetPassword" -> ResetPasswordScreen(
                onBackClick = { currentScreen = "Login" }
            )
            "SignUp" -> SignUpScreen(
                onBackClick = { currentScreen = "Login" }
            )
        }
    }
}

// Login Screen with trendy gradient background
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CandyPink,  SkyBlue, GradientWhite)
                    )
                )
                .padding(16.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (email.isEmpty()) {
                        Text("Email", fontSize = 20.sp, color = Color.White)
                    }
                    innerTextField()
                }
            }
        )
        if (emailError) {
            Text("Invalid email format", color = Color.White, fontSize = 16.sp)
        }

        BasicTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CandyPink, SkyBlue, GradientWhite)
                    )
                )
                .padding(16.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (password.isEmpty()) {
                        Text("Password", fontSize = 20.sp, color = Color.White)
                    }
                    innerTextField()
                }
            }
        )
        if (passwordError) {
            Text("Password cannot be empty", color = Color.White, fontSize = 16.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = passwordVisible, onCheckedChange = { passwordVisible = it })
            Text("Show Password", color = Color.White)
        }

        Button(
            onClick = {
                emailError = !isValidEmail(email)
                passwordError = password.isEmpty()
                if (!emailError && !passwordError) {
                    onLoginSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CandyPink)
        ) {
            Text("Log In", fontSize = 20.sp, color = Color.Black)
        }

        TextButton(onClick = onSignUpClick) {
            Text("Sign Up", color = Color.White, fontSize = 18.sp)
        }

        TextButton(onClick = onForgotPasswordClick) {
            Text("Forgot Password?", color = Color.White, fontSize = 18.sp)
        }
    }
}

// Home Screen with trendy gradient background
@Composable
fun HomeScreen(onLogoutClick: () -> Unit) {
    val productList = listOf(
        Product("PALETTE H3", " 6 fonds de teint crème, 4 blush crème et 2 highlighters pour unifier, colorer et mettre en valeur vos joues.", painterResource(id = R.drawable.img1)),
        Product("ROUGE ARTIST METALLICS", "Dessinez tous vos looks avec le crayon ARTIST COLOR PENCIL", painterResource(id = R.drawable.img2)),
        Product("ARTIST COLOR PENCIL", "Composez des looks à l’infini avec nos nouvelles palettes ARTIST COLOR PRO", painterResource(id = R.drawable.img3)),
        Product("HD SKIN", "Le fond de teint longue tenue imperceptible qui fusionne avec la peau ", painterResource(id = R.drawable.img4)),
        Product("ARTIST HYDRABLOOM", "Ce baume à lèvres universel 24 heures ", painterResource(id = R.drawable.img5)),


        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Shop", fontSize = 24.sp, color = Color.White)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(productList) { product ->
                ProductCard(product = product)
            }
        }

        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
        ) {
            Text("Logout", fontSize = 20.sp, color = Color.Black)
        }
    }
}

// Product Card with trendy gradient background
@Composable
fun ProductCard(product: Product) {
    var showDescription by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = product.image,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(product.name, fontSize = 20.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(8.dp))

            // Modification de la couleur de fond du bouton
            Button(
                onClick = { showDescription = !showDescription },
                colors = ButtonDefaults.buttonColors(containerColor = CandyPink)
            ) {
                Text(
                    text = if (showDescription) "Hide Description" else "Show Description",
                    color = Color.Black // Texte noir pour contraste
                )
            }

            if (showDescription) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(product.description, fontSize = 16.sp, color = Color.Black)
            }
        }
    }
}


// Reset Password Screen with trendy gradient background
@Composable
fun ResetPasswordScreen(onBackClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var emailSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reset Password", fontSize = 30.sp, color = Color.White)

        if (!emailSent) {
            BasicTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CandyPink, SkyBlue, GradientWhite )
                        )
                    )
                    .padding(16.dp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (email.isEmpty()) {
                            Text("Email", fontSize = 20.sp, color = Color.White)
                        }
                        innerTextField()
                    }
                }
            )
            if (emailError) {
                Text("Invalid email format", color = Color.White, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    emailError = !isValidEmail(email)
                    if (!emailError) {
                        emailSent = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
            ) {
                Text("Send Reset Email", fontSize = 20.sp, color = Color.Black)
            }
        } else {
            Text("Email sent! Please check your inbox.", color = Color.White, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor =  CandyPink)) {
            Text("Back to Login", fontSize = 20.sp, color = Color.Black)
        }
    }
}

@Composable
fun SignUpScreen(onBackClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign Up", fontSize = 30.sp, color = Color.White)

        BasicTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf( CandyPink,  SkyBlue, GradientWhite)
                    )
                )
                .padding(16.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (name.isEmpty()) {
                        Text("Full Name", fontSize = 20.sp, color = Color.White)
                    }
                    innerTextField()
                }
            }
        )

        BasicTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CandyPink, SkyBlue, GradientWhite)
                    )
                )
                .padding(16.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (email.isEmpty()) {
                        Text("Email", fontSize = 20.sp, color = Color.White)
                    }
                    innerTextField()
                }
            }
        )
        if (emailError) {
            Text("Invalid email format", color = Color.White, fontSize = 16.sp)
        }

        BasicTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CandyPink, SkyBlue, GradientWhite)
                    )
                )
                .padding(16.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (password.isEmpty()) {
                        Text("Password", fontSize = 20.sp, color = Color.White)
                    }
                    innerTextField()
                }
            }
        )
        if (passwordError) {
            Text("Password cannot be empty", color = Color.White, fontSize = 16.sp)
        }

        BasicTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CandyPink, SkyBlue, GradientWhite)
                    )
                )
                .padding(16.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (confirmPassword.isEmpty()) {
                        Text("Confirm Password", fontSize = 20.sp, color = Color.White)
                    }
                    innerTextField()
                }
            }
        )
        if (confirmPasswordError) {
            Text("Passwords do not match", color = Color.White, fontSize = 16.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = passwordVisible, onCheckedChange = { passwordVisible = it })
            Text("Show Password", color = Color.White)
        }

        Button(
            onClick = {
                emailError = !isValidEmail(email)
                passwordError = password.isEmpty()
                confirmPasswordError = confirmPassword != password
                if (!emailError && !passwordError && !confirmPasswordError) {
                    saveUserData(context,name,email,password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
        ) {
            Text("Sign Up", fontSize = 20.sp, color = Color.Black)
        }

        TextButton(onClick = onBackClick) {
            Text("Back to Login", color = Color.White, fontSize = 18.sp)
        }
    }
}

// Helper function to validate email format
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen()
}
