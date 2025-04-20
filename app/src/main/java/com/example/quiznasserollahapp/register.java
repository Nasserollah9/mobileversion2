package com.example.quiznasserollahapp;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class register extends AppCompatActivity {
    EditText etMail, etPassword, etPassword1;
    Button bRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null) {
            Intent intent=new Intent (register.this,login.class);
            startActivity(intent);
            finish();
        }
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();
        etMail=(EditText) findViewById(R.id.etMail);
        etPassword=(EditText) findViewById(R.id.etPassword);
        etPassword1=(EditText)findViewById(R.id.etPassword1);
        bRegister=(Button)findViewById(R.id.bRegister);
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                String mail=etMail.getText().toString();
                String password=etPassword.getText().toString();
                String password1=etPassword1.getText().toString();
                if(TextUtils.isEmpty(mail)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password1)){
                    Toast.makeText(getApplicationContext(),"Please confirm your password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length()<6){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!password.equals(password1)){
                    Toast.makeText(getApplicationContext(),"Please enter correct password",Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(task ->
                {if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),"Registration Successful!  inscription réussi! التسجيل ناجح! 註冊成功  ",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(register.this,login.class));
                    finish();}
                else{
                    Toast.makeText(getApplicationContext(),"Registration failde!" + task.getException().getMessage(),Toast.LENGTH_LONG).show();

                }
                });
                //Commen.login=Mail;
                //Commen.password=password;

            }
        });
    }
}