package com.example.projetojava;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    // Lista de produtos e contexto da aplicação
    private List<Product> productList;
    private Context context;
    private DatabaseHelper dbHelper;

    // Construtor: recebe o contexto e a lista de produtos
    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.dbHelper = new DatabaseHelper(context); // Inicializa o helper da base de dados
    }

    // Cria e retorna uma nova instância do ViewHolder
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item de produto
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    // Associa os dados do produto ao ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Obtém o produto na posição atual
        Product product = productList.get(position);

        // Define o nome e o preço do produto no layout
        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format(Locale.getDefault(), "R$ %.2f", product.getPrice()));

        // Configura o botão de atualização
        holder.updateButton.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                // Chama o método da MainActivity para exibir o diálogo de atualização
                ((MainActivity) context).showUpdateProductDialog(product);
            }
        });

        // Configura o botão de exclusão
        holder.deleteButton.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                // Chama o método da MainActivity para excluir o produto
                ((MainActivity) context).deleteProduct(product.getId());
            }
        });
    }

    // Retorna o número de itens na lista de produtos
    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Método para atualizar a lista de produtos e notificar o RecyclerView
    public void updateProductList(List<Product> newProductList) {
        this.productList.clear(); // Limpa a lista atual
        this.productList.addAll(newProductList); // Adiciona a nova lista
        notifyDataSetChanged(); // Notifica o RecyclerView sobre as alterações
    }

    // Classe interna para representar o ViewHolder dos produtos
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        // Componentes visuais para exibir informações do produto
        TextView productName, productPrice;
        Button updateButton, deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializa os componentes do layout
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            updateButton = itemView.findViewById(R.id.update_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    // Método para atualizar um produto na base de dados
    public void updateProduct(int id, String newName, double newPrice) {
        // Abre a base de dados no modo escrita
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Executa a instrução SQL para atualizar os dados do produto
        db.execSQL("UPDATE products SET name = ?, price = ? WHERE id = ?", new Object[]{newName, newPrice, id});
        db.close(); // Fecha a base de dados após a operação
    }

    // Método para excluir um produto da base de dados
    public void deleteProduct(int id) {
        // Abre a base de dados no modo escrita
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Executa a instrução SQL para excluir o produto pelo ID
        db.execSQL("DELETE FROM products WHERE id = ?", new Object[]{id});
        db.close(); // Fecha a base de dados após a operação
    }
}
