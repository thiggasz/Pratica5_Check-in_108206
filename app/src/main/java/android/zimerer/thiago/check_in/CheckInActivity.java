package android.zimerer.thiago.check_in;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CheckInActivity extends AppCompatActivity {
    private BancoDados bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("GestãoCheckIn");

        bd = new BancoDados(this);

        populateCheckins();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it = null;
        if (item.getItemId() == R.id.back) {
            it = new Intent(getBaseContext(), MainActivity.class);
        }

        if(it != null) {
            startActivity(it);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateCheckins() {
        LinearLayout locationsLayout = findViewById(R.id.locationsLayout);
        LinearLayout deleteLayout = findViewById(R.id.timesVisitedLayout);

        clear(locationsLayout);
        clear(deleteLayout);

        Cursor cursor = bd.buscarCheckinsCategoria();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String local = cursor.getString(cursor.getColumnIndexOrThrow("Local"));

                TextView tvLocal = createTextView(local, Gravity.START);
                locationsLayout.addView(tvLocal);

                Button btnDelete = createDeleteButton();
                deleteLayout.addView(btnDelete);

                btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(local));

            } while (cursor.moveToNext());
            cursor.close();
        }
        bd.fechar();
    }

    private TextView createTextView(String text, int gravity) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setGravity(gravity);
        textView.setTextSize(16);
        textView.setPadding(0, 18, 0, 8);
        return textView;
    }

    private void clear(LinearLayout layout) {
        if (layout.getChildCount() > 1) {
            layout.removeViews(1, layout.getChildCount() - 1);
        }
    }

    private Button createDeleteButton() {
        Button button = new Button(this);
        button.setText("🚫");
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(4, 0, 4, 0);
        button.setGravity(Gravity.END);

        button.setBackground(null);
        button.setBackgroundResource(android.R.color.transparent);

        return button;
    }

    private void showDeleteConfirmationDialog(String local) {
        new AlertDialog.Builder(this)
                .setTitle("Exclusão")
                .setMessage("Tem certeza que deseja excluir " + local + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    bd.abrir(CheckInActivity.this);
                    bd.deletar("Checkin", "Local = '" + local + "'");
                    bd.fechar();

                    populateCheckins();
                    finish();
                    startActivity(getIntent());

                    Toast.makeText(CheckInActivity.this, "Local excluído!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}