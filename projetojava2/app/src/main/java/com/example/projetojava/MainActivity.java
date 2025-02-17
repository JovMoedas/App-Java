package com.example.projetojava;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    private Button logoutButton, addProductButton;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private DatabaseHelper dbHelper;
    private List<Product> productList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializa os componentes da interface
        welcomeMessage = findViewById(R.id.welcome_message);
        logoutButton = findViewById(R.id.logout_button);
        addProductButton = findViewById(R.id.add_product_button);
        recyclerView = findViewById(R.id.recycler_view);

        // Configura o RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);

        // Recupera e exibe o nome do utilizador
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        welcomeMessage.setText(getString(R.string.welcome_message, username));

        // Configura o botão de logout
        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        // Configura o botão para adicionar produtos
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        // Carrega os produtos ao iniciar
        loadProductsFromDatabase();
    }

    // Exibe o diálogo para adicionar um novo produto
    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        EditText productNameInput = dialogView.findViewById(R.id.product_name_input);
        EditText productPriceInput = dialogView.findViewById(R.id.product_price_input);
        Button saveButton = dialogView.findViewById(R.id.save_product_button);

        AlertDialog dialog = builder.create();
        saveButton.setOnClickListener(v -> {
            String productName = productNameInput.getText().toString().trim();
            String productPriceStr = productPriceInput.getText().toString().trim();

            if (productName.isEmpty() || productPriceStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double productPrice = Double.parseDouble(productPriceStr);
                    insertProductIntoDatabase(productName, productPrice);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.error_invalid_price), Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    // Insere um novo produto na base de dados
    private void insertProductIntoDatabase(String name, double price) {
        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("price", price);

        long result = db.insert("products", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, getString(R.string.success_insert_product), Toast.LENGTH_SHORT).show();
            loadProductsFromDatabase();
        } else {
            Toast.makeText(this, getString(R.string.error_insert_product), Toast.LENGTH_SHORT).show();
        }
    }

    // Carrega todos os produtos da base de dados
    private void loadProductsFromDatabase() {
        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products", null);

        productList.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            double price = cursor.getDouble(2);
            productList.add(new Product(id, name, price));
        }
        cursor.close();
        db.close();

        productAdapter.notifyDataSetChanged();
    }

    // Exibe o diálogo de edição com os dados pré-preenchidos
    public void showUpdateProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        EditText productNameInput = dialogView.findViewById(R.id.product_name_input);
        EditText productPriceInput = dialogView.findViewById(R.id.product_price_input);
        Button saveButton = dialogView.findViewById(R.id.save_product_button);

        // Preenche os campos com os dados atuais
        productNameInput.setText(product.getName());
        productPriceInput.setText(String.valueOf(product.getPrice()));

        AlertDialog dialog = builder.create();
        saveButton.setOnClickListener(v -> {
            String newName = productNameInput.getText().toString().trim();
            String newPriceStr = productPriceInput.getText().toString().trim();

            if (newName.isEmpty() || newPriceStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double newPrice = Double.parseDouble(newPriceStr);
                    updateProductInDatabase(product.getId(), newName, newPrice);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.error_invalid_price), Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    // Atualiza um produto existente na base de dados
    public void updateProductInDatabase(int id, String newName, double newPrice) {
        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("price", newPrice);

        int result = db.update("products", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();

        if (result > 0) {
            Toast.makeText(this, getString(R.string.success_update_product), Toast.LENGTH_SHORT).show();
            loadProductsFromDatabase();
        } else {
            Toast.makeText(this, getString(R.string.error_update_product), Toast.LENGTH_SHORT).show();
        }
    }

    // Elimina um produto da base de dados
    public void deleteProduct(int id) {
        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("products", "id = ?", new String[]{String.valueOf(id)});
        db.close();

        if (rowsDeleted > 0) {
            Toast.makeText(this, getString(R.string.success_delete_product), Toast.LENGTH_SHORT).show();
            loadProductsFromDatabase();
        } else {
            Toast.makeText(this, getString(R.string.error_delete_product), Toast.LENGTH_SHORT).show();
        }
    }
}
