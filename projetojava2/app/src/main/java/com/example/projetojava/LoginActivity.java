package com.example.projetojava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // Declaração do helper para interagir com a base de dados
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se o utilizador já está logado
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        // Se o utilizador já estiver logado, redireciona para a MainActivity
        if (isLoggedIn) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Associa a interface gráfica com a classe através do layout correspondente
        setContentView(R.layout.activity_main);

        // Inicializa o helper da base de dados
        dbHelper = new DatabaseHelper(this);

        // Liga os componentes visuais aos respetivos IDs
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        EditText editUsername = findViewById(R.id.editUsername);
        EditText editPassword = findViewById(R.id.editPassword);
        Button btnSave = findViewById(R.id.btnSave);

        // Configuração do adaptador para gerir os fragmentos do ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new LoginFragment(), getString(R.string.login_tab));
        adapter.addFragment(new SignupFragment(), getString(R.string.signup_tab));

        // Define o adaptador no ViewPager para exibir as abas de login e registo
        viewPager2.setAdapter(adapter);

        // Configuração da interação entre o TabLayout e o ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            String title = adapter.getPageTitle(position);
            // Define o texto da aba, utilizando um valor padrão se for nulo
            tab.setText(title != null ? title : getString(R.string.default_tab_name));
        }).attach();

        // Configura o comportamento do botão "Salvar"
        btnSave.setOnClickListener(v -> {
            // Recolhe os dados inseridos pelo utilizador
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // Verifica se os campos não estão vazios
            if (!username.isEmpty() && !password.isEmpty()) {
                insertUser(username, password);
            } else {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método responsável por inserir um novo utilizador na base de dados
    private void insertUser(String username, String password) {
        // Abre a base de dados no modo escrita
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Cria um conjunto de valores para a inserção
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        // Verifica se os campos são válidos
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
            return;
        }

        // Tenta inserir o utilizador na base de dados
        long result = db.insert("users", null, values);
        db.close();

        // Mostra uma mensagem de sucesso ou erro dependendo do resultado da operação
        if (result != -1) {
            Toast.makeText(this, getString(R.string.success_insert_user), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.error_insert_user), Toast.LENGTH_SHORT).show();
        }
    }

    // Classe interna para gerir o adaptador de fragmentos no ViewPager2
    private class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> titles = new ArrayList<>();

        // Construtor da classe
        public ViewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        // Adiciona fragmentos e respetivos títulos ao adaptador
        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        // Cria os fragmentos consoante a posição
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        // Retorna o número total de fragmentos geridos pelo adaptador
        @Override
        public int getItemCount() {
            return fragments.size();
        }

        // Obtém o título de uma aba específica
        public String getPageTitle(int position) {
            // Verifica se a posição é válida e retorna o título correspondente
            return (position >= 0 && position < titles.size()) ? titles.get(position) : getString(R.string.default_tab_name);
        }
    }
}
