package com.example.localservice.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.localservice.R
import com.example.localservice.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException

// Botón "Continuar con Google" reutilizable en Login y Register.
// Lanza el flujo de Google Sign-In y delega el resultado al AuthViewModel.
@Composable
fun GoogleSignInButton(
    viewModel: AuthViewModel,
    enabled: Boolean = true
) {
    val context = LocalContext.current

    val googleSignInClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.signInWithGoogle(account)
        } catch (e: ApiException) {
            // Solo avisamos si no fue una cancelación del usuario
            if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                Toast.makeText(context, "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    OutlinedButton(
        onClick = {
            launcher.launch(googleSignInClient.signInIntent)
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text("Continuar con Google")
    }
}
