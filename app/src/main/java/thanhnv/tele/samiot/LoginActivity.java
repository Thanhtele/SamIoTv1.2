package thanhnv.tele.samiot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends Activity {
    private FirebaseAuth auth;
    private EditText inEmail,inPassword;
    private Button loginBtn;
    private ImageView loginWithGG;
    private TextView forgotPass, registerAcc;
    private ProgressBar progressing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth =FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.login);

        inEmail = (EditText) findViewById(R.id.email_sig);
        inPassword = (EditText) findViewById(R.id.password_sig);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginWithGG = (ImageButton) findViewById(R.id.loginGG);
        progressing = (ProgressBar) findViewById(R.id.progressBar);
        forgotPass = (TextView) findViewById(R.id.forgotpw);
        registerAcc = (TextView) findViewById(R.id.register);

        registerAcc.setPaintFlags(registerAcc.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgotPass.setPaintFlags(forgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        auth = FirebaseAuth.getInstance();
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inEmail.getText().toString();
                final String password = inPassword.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressing.setVisibility((View.VISIBLE));

                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener< AuthResult >() {
                    @Override
                    public void onComplete(@NonNull Task< AuthResult > task) {
                        progressing.setVisibility(View.GONE);
                        if(!task.isSuccessful()){
                            if(password.length() < 6 ){
                                inPassword.setError("Password must be at least 6 characters long ");
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "Authentication failed, check your email and password or sign up", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });

    }
}
