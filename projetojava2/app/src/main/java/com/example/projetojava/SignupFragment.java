package com.example.projetojava;

import android.annotation.SuppressLint;
import android.content.ContentValues;
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

public class SignupFragment extends Fragment {

    // Declaração dos campos de entrada, mensagens de erro e helper da base de dados
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private TextView errorMessageTextView;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout associado a este fragmento
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Inicializa os campos de entrada com os IDs definidos no layout
        usernameEditText = view.findViewById(R.id.signup_username);
        passwordEditText = view.findViewById(R.id.signup_password);
        confirmPasswordEditText = view.findViewById(R.id.signup_confirm_password);
        errorMessageTextView = view.findViewById(R.id.error_message);

        // Inicializa o botão de registo
        Button signupButton = view.findViewById(R.id.signup_button);

        // Inicializa a base de dados
        dbHelper = new DatabaseHelper(requireContext());

        // Configura o comportamento ao clicar no botão de registo
        signupButton.setOnClickListener(v -> {
            // Obtem os valores inseridos pelo utilizador
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // Validação: verifica se há campos vazios
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errorMessageTextView.setText("Por favor, preencha todos os campos.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
            // Validação: verifica se as senhas são iguais
            else if (!password.equals(confirmPassword)) {
                errorMessageTextView.setText("As senhas não coincidem.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
            // Se as validações forem bem-sucedidas, insere o utilizador na base de dados
            else {
                insertUser(username, password);
            }
        });

        // Retorna a interface criada
        return view;
    }

    // Método responsável por inserir um novo utilizador na base de dados
    private void insertUser(String username, String password) {
        // Verifica se o utilizador já existe
        if (userExists(username)) {
            // Se o utilizador existir, mostra uma mensagem de erro e interrompe o registo
            Toast.makeText(requireContext(), getString(R.string.error_user_already_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria um novo registo na base de dados com o utilizador e senha fornecidos
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Preenche os campos necessários para o registo
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        // Executa a inserção na tabela 'users'
        long result = db.insert("users", null, values);
        db.close();

        // Verifica se a inserção foi bem-sucedida
        if (result != -1) {
            Toast.makeText(requireContext(), getString(R.string.success_insert_user), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_insert_user), Toast.LENGTH_SHORT).show();
        }
    }

    // Método responsável por verificar se um utilizador já existe na base de dados
    private boolean userExists(String username) {
        // Abre a base de dados no modo leitura
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Executa uma consulta para procurar o utilizador com o nome fornecido
        Cursor cursor = db.rawQuery("SELECT 1 FROM users WHERE username = ?", new String[]{username});

        // O método moveToFirst() retorna true se o cursor encontrar algum registo
        boolean exists = cursor.moveToFirst();

        // Fecha o cursor e a base de dados para evitar vazamentos de memória
        cursor.close();
        db.close();

        // Retorna true se o utilizador existir, false caso contrário
        return exists;
    }
}
