package com.example.projetojava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    // Declaração de variáveis para os componentes da interface
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView errorMessageTextView;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout da interface gráfica associado ao fragmento de login
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Inicializa os componentes da interface gráfica
        usernameEditText = view.findViewById(R.id.username);
        passwordEditText = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.login_button);
        errorMessageTextView = view.findViewById(R.id.error_message);

        // Inicializa o helper da base de dados para interagir com os dados locais
        dbHelper = new DatabaseHelper(requireContext());

        // Configura o comportamento do botão de login
        loginButton.setOnClickListener(v -> {
            // Recolhe os dados fornecidos pelo utilizador
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Valida se os campos foram preenchidos
            if (username.isEmpty() || password.isEmpty()) {
                errorMessageTextView.setText("Por favor, preencha todos os campos.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            } else {
                // Verifica as credenciais fornecidas
                checkLogin(username, password);
            }
        });

        // Retorna a interface construída para o fragmento
        return view;
    }

    // Método responsável por verificar o login do utilizador
    private void checkLogin(String username, String password) {
        // Abre a base de dados no modo leitura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta a base de dados para verificar se existem credenciais correspondentes
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?",
                new String[]{username, password});

        // Se a consulta retornar resultados, o login é bem-sucedido
        if (cursor.moveToFirst()) {
            Toast.makeText(requireContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show();

            // Cria um Intent para abrir a MainActivity (home) após o login
            Intent intent = new Intent(requireContext(), MainActivity.class);
            // Passa o nome do utilizador como parâmetro para a atividade seguinte
            intent.putExtra("username", String.valueOf(username));
            startActivity(intent);

            // Finaliza a atividade atual para evitar o regresso ao ecrã de login
            requireActivity().finish();
        } else {
            // Exibe uma mensagem de erro caso as credenciais estejam incorretas
            errorMessageTextView.setText("Credenciais inválidas. Tente novamente.");
            errorMessageTextView.setVisibility(View.VISIBLE);
        }

        // Fecha o cursor e a base de dados para libertar recursos
        cursor.close();
        db.close();
    }
}
